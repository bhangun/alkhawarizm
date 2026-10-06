package tech.kayys.alkhawarizm.gguf.javabackend;

import tech.kayys.alkhawarizm.gguf.loader.GGUFModel;

import java.util.List;
import java.util.Map;

/**
 * Transformer hyperparameters read straight out of {@link GGUFModel#metadata()}.
 *
 * <p>This intentionally does NOT reuse either of the two {@code ModelConfig}
 * classes already in the codebase:</p>
 * <ul>
 *   <li>{@code tech.kayys.alkhawarizm.gguf.model.ModelConfig} (a record) is
 *       only ever constructed from a {@code GGUFFile}, a different loaded-file
 *       representation than the {@code GGUFModel} this runner actually
 *       produces — there is no {@code fromGGUFModel} path for it.</li>
 *   <li>The {@code ModelConfig} that {@code LlamaForward}/{@code LlamaWeights}
 *       expect (bare, unimported, in their own {@code gguf.loader.model}
 *       package) doesn't resolve to any class in this codebase at all — see
 *       the migration notes for the full account.</li>
 * </ul>
 *
 * <p>Also fixes a real correctness gap in both of those: neither reads keys
 * prefixed by the model's actual {@code general.architecture} (they either
 * hardcode {@code "llama."} or don't prefix at all), which silently breaks
 * on Qwen/Mistral/Gemma-family GGUF files where hyperparameters live under
 * e.g. {@code qwen2.*} instead of {@code llama.*}.</p>
 */
record GgufModelConfig(
        String architecture,
        int contextLength,
        int embeddingDim,
        int nLayers,
        int ffnDim,
        int nHeads,
        int nKVHeads,
        int headDim,
        int vocabSize,
        float rmsNormEps,
        float ropeFreqBase,
        int bosTokenId,
        int eosTokenId) {

    static GgufModelConfig from(GGUFModel model) {
        Map<String, Object> meta = model.metadata();
        String arch = String.valueOf(meta.getOrDefault("general.architecture", "llama"));

        int embeddingDim = intMeta(meta, arch, "embedding_length", 4096);
        int nHeads = intMeta(meta, arch, "attention.head_count", 32);
        int nKVHeads = intMeta(meta, arch, "attention.head_count_kv", nHeads);
        int defaultHeadDim = nHeads == 0 ? embeddingDim : embeddingDim / nHeads;
        int headDim = intMeta(meta, arch, "attention.key_length", defaultHeadDim);

        return new GgufModelConfig(
                arch,
                intMeta(meta, arch, "context_length", 4096),
                embeddingDim,
                intMeta(meta, arch, "block_count", 32),
                intMeta(meta, arch, "feed_forward_length", embeddingDim * 4),
                nHeads,
                nKVHeads,
                headDim,
                vocabSizeOf(meta),
                floatMeta(meta, arch, "attention.layer_norm_rms_epsilon", 1e-5f),
                floatMeta(meta, arch, "rope.freq_base", 10000f),
                intOf(meta, "tokenizer.ggml.bos_token_id", 1),
                intOf(meta, "tokenizer.ggml.eos_token_id", 2));
    }

    private static int vocabSizeOf(Map<String, Object> meta) {
        Object tokens = meta.getOrDefault("tokenizer.ggml.tokens", meta.get("tokenizer.tokens"));
        return tokens instanceof List<?> list ? list.size() : 32000;
    }

    private static int intMeta(Map<String, Object> meta, String arch, String suffix, int fallback) {
        return intOf(meta, arch + "." + suffix, fallback);
    }

    private static float floatMeta(Map<String, Object> meta, String arch, String suffix, float fallback) {
        return floatOf(meta, arch + "." + suffix, fallback);
    }

    private static int intOf(Map<String, Object> meta, String key, int fallback) {
        Object v = meta.get(key);
        return v instanceof Number n ? n.intValue() : fallback;
    }

    private static float floatOf(Map<String, Object> meta, String key, float fallback) {
        Object v = meta.get(key);
        return v instanceof Number n ? n.floatValue() : fallback;
    }
}
