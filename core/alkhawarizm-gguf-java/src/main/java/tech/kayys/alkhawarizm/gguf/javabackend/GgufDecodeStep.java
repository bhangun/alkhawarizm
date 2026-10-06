package tech.kayys.alkhawarizm.gguf.javabackend;

import tech.kayys.alkhawarizm.gguf.loader.GGUFModel;
import tech.kayys.alkhawarizm.gguf.loader.GGUFTensorInfo;
import tech.kayys.alkhawarizm.gguf.loader.inference.KVCache;
import tech.kayys.alkhawarizm.gguf.loader.tensor.GGUFVectorOps;
import tech.kayys.alkhawarizm.gguf.runtime.GgufTensorOps;

import java.util.Optional;

/**
 * One decoder forward pass: {@code token, position -> logits}, given a
 * running {@link KVCache}.
 *
 * <p>Same algorithm shape as the existing (currently unreachable, dead)
 * {@code gguf.model.LlamaForward}: embed, N transformer blocks (RMSNorm ->
 * QKV -> RoPE -> causal GQA attention -> output proj -> residual -> RMSNorm
 * -> SwiGLU/silu FFN -> residual), final RMSNorm, output projection. It is
 * rehosted here on {@link GgufTensorOps}/{@link GGUFModel} — the type stack
 * the runner, {@code GgufRuntimeProfile}, and {@code GGUFTokenizer} already
 * use — instead of {@code LlamaForward}'s {@code GGUFFile}/
 * {@code gguf.core.GGUFTensorInfo}/{@code Dequantizer} stack, which
 * {@code GGUFModel} cannot produce (see the migration notes for why those
 * two stacks can't just be glued together).</p>
 *
 * <p>Two concrete bugs in the code this was ported from are fixed here,
 * not carried forward:</p>
 * <ul>
 *   <li>{@code LlamaForward.forward()} never called {@code cache.advance()}.
 *       {@link KVCache#store} writes at the cache's own internal
 *       {@code seqLen}, which only moves forward via {@code advance()} — so
 *       every token was silently overwriting position 0 instead of
 *       accumulating history. Fixed: {@link #forward} calls
 *       {@code cache.advance()} exactly once, after all layers for the
 *       current position have stored their K/V.</li>
 *   <li>{@code cfg.getRmsNormEps()} / {@code cfg.getVocabSize()} were called
 *       against a {@code ModelConfig} type that doesn't declare those
 *       methods under any resolvable package. Fixed by using
 *       {@link GgufModelConfig}'s plain record accessors throughout.</li>
 * </ul>
 *
 * <p>Weight tensors are resolved once at construction via
 * {@link GgufTensorOps#findTensor}, using the standard llama.cpp GGUF naming
 * convention ({@code token_embd.weight}, {@code blk.N.attn_*},
 * {@code blk.N.ffn_*}, {@code output_norm.weight}, {@code output.weight}).
 * Norm vectors are small (one row of {@code embeddingDim} floats) and are
 * dequantized once eagerly; every large matrix (attention and FFN
 * projections, the output head) stays in its quantized, mmap'd form and
 * goes through {@link GgufTensorOps#matVec} per call — there is no eager
 * whole-model dequantization here, unlike the old {@code LlamaWeights.load()}.</p>
 */
final class GgufDecodeStep {
    private final GGUFModel model;
    private final GgufModelConfig cfg;

    private final GGUFTensorInfo tokenEmbed;
    private final float[] outputNorm;
    private final GGUFTensorInfo outputWeight;
    private final float[][] attnNorm;
    private final GGUFTensorInfo[] wQ, wK, wV, wO;
    private final float[][] ffnNorm;
    private final GGUFTensorInfo[] ffnGate, ffnUp, ffnDown;

    // Scratch buffers reused across calls to avoid per-token allocation
    // (see production roadmap Tier 3, item 12 — this is that fix, applied
    // at construction time rather than left as a later optimization pass).
    private final float[] x, xb, q, k, v, attnFull, attnOut, gate, up, hb, scores;

    GgufDecodeStep(GGUFModel model, GgufModelConfig cfg) {
        this.model = model;
        this.cfg = cfg;

        int dim = cfg.embeddingDim();
        int ffnDim = cfg.ffnDim();
        int nH = cfg.nHeads();
        int nKVH = cfg.nKVHeads();
        int hDim = cfg.headDim();
        int layers = cfg.nLayers();

        this.tokenEmbed = requireTensor("token_embd.weight");
        this.outputNorm = dequantizeVector(requireTensor("output_norm.weight"), dim);
        this.outputWeight = findTensor("output.weight").orElse(tokenEmbed); // tied embeddings

        this.attnNorm = new float[layers][];
        this.wQ = new GGUFTensorInfo[layers];
        this.wK = new GGUFTensorInfo[layers];
        this.wV = new GGUFTensorInfo[layers];
        this.wO = new GGUFTensorInfo[layers];
        this.ffnNorm = new float[layers][];
        this.ffnGate = new GGUFTensorInfo[layers];
        this.ffnUp = new GGUFTensorInfo[layers];
        this.ffnDown = new GGUFTensorInfo[layers];

        for (int i = 0; i < layers; i++) {
            String p = "blk." + i + ".";
            attnNorm[i] = dequantizeVector(requireTensor(p + "attn_norm.weight"), dim);
            wQ[i] = requireTensor(p + "attn_q.weight");
            wK[i] = requireTensor(p + "attn_k.weight");
            wV[i] = requireTensor(p + "attn_v.weight");
            wO[i] = requireTensor(p + "attn_output.weight");
            ffnNorm[i] = dequantizeVector(requireTensor(p + "ffn_norm.weight"), dim);
            ffnGate[i] = findTensor(p + "ffn_gate.weight").orElse(null); // absent on some archs (plain MLP, no gate)
            ffnUp[i] = requireTensor(p + "ffn_up.weight");
            ffnDown[i] = requireTensor(p + "ffn_down.weight");
        }

        this.x = new float[dim];
        this.xb = new float[dim];
        this.q = new float[nH * hDim];
        this.k = new float[nKVH * hDim];
        this.v = new float[nKVH * hDim];
        this.attnFull = new float[nH * hDim];
        this.attnOut = new float[hDim];
        this.gate = new float[ffnDim];
        this.up = new float[ffnDim];
        this.hb = new float[ffnDim];
        this.scores = new float[cfg.contextLength()];
    }

    /** One token's forward pass. Returns a freshly-allocated {@code vocabSize} logits array. */
    float[] forward(int tokenId, int pos, KVCache cache) {
        int dim = cfg.embeddingDim();
        int nH = cfg.nHeads();
        int nKVH = cfg.nKVHeads();
        int hDim = cfg.headDim();
        int ffnDim = cfg.ffnDim();
        float eps = cfg.rmsNormEps();

        GgufTensorOps.dequantizeRow(model, tokenEmbed, tokenId, x);

        for (int layer = 0; layer < cfg.nLayers(); layer++) {
            GGUFVectorOps.rmsNorm(x, attnNorm[layer], xb, dim, eps);

            GgufTensorOps.matVec(model, wQ[layer], xb, q);
            GgufTensorOps.matVec(model, wK[layer], xb, k);
            GgufTensorOps.matVec(model, wV[layer], xb, v);

            GGUFVectorOps.rope(q, nH, hDim, pos, cfg.ropeFreqBase());
            GGUFVectorOps.rope(k, nKVH, hDim, pos, cfg.ropeFreqBase());

            cache.store(layer, k, v); // writes at cache's current seqLen; advanced once below

            float scale = (float) (1.0 / Math.sqrt(hDim));
            int seqLen = pos + 1;

            GGUFVectorOps.zero(attnFull, nH * hDim);

            for (int h = 0; h < nH; h++) {
                int kvH = h / (nH / nKVH);
                int qOff = h * hDim;

                float[] kCache = cache.kLayer(layer);
                for (int t = 0; t < seqLen; t++) {
                    int kBase = t * cache.headStride() + kvH * hDim;
                    scores[t] = GGUFVectorOps.dot(q, qOff, kCache, kBase, hDim) * scale;
                }

                GGUFVectorOps.softmax(scores, seqLen);

                GGUFVectorOps.zero(attnOut, hDim);
                float[] vCache = cache.vLayer(layer);
                for (int t = 0; t < seqLen; t++) {
                    int vBase = t * cache.headStride() + kvH * hDim;
                    GGUFVectorOps.scaledAdd(attnOut, attnOut, scores[t], vCache, vBase, hDim);
                }

                System.arraycopy(attnOut, 0, attnFull, h * hDim, hDim);
            }

            GgufTensorOps.matVec(model, wO[layer], attnFull, xb);
            GGUFVectorOps.addInPlace(x, xb, dim);

            GGUFVectorOps.rmsNorm(x, ffnNorm[layer], xb, dim, eps);

            if (ffnGate[layer] != null) {
                GgufTensorOps.matVec(model, ffnGate[layer], xb, gate);
                GgufTensorOps.matVec(model, ffnUp[layer], xb, up);
                GGUFVectorOps.swiGLU(gate, up, hb, ffnDim);
            } else {
                GgufTensorOps.matVec(model, ffnUp[layer], xb, up);
                GGUFVectorOps.siluInPlace(up, ffnDim);
                System.arraycopy(up, 0, hb, 0, ffnDim);
            }

            GgufTensorOps.matVec(model, ffnDown[layer], hb, xb);
            GGUFVectorOps.addInPlace(x, xb, dim);
        }

        cache.advance(); // fix: this call was missing entirely in the code ported from

        GGUFVectorOps.rmsNorm(x, outputNorm, xb, dim, eps);

        float[] logits = new float[cfg.vocabSize()];
        GgufTensorOps.matVec(model, outputWeight, xb, logits);
        return logits;
    }

    private GGUFTensorInfo requireTensor(String name) {
        return findTensor(name)
                .orElseThrow(() -> new IllegalStateException(
                        "GGUF model is missing required tensor '" + name + "' for architecture '"
                                + cfg.architecture() + "'"));
    }

    private Optional<GGUFTensorInfo> findTensor(String name) {
        return Optional.ofNullable(GgufTensorOps.findTensor(model, name));
    }

    private float[] dequantizeVector(GGUFTensorInfo tensor, int dim) {
        float[] out = new float[dim];
        GgufTensorOps.dequantizeRow(model, tensor, 0L, out);
        return out;
    }
}
