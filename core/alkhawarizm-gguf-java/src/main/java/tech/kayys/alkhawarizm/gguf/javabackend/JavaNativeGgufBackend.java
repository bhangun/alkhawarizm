package tech.kayys.alkhawarizm.gguf.javabackend;

import tech.kayys.alkhawarizm.gguf.api.GgufBackend;
import tech.kayys.alkhawarizm.gguf.loader.GGUFModel;
import tech.kayys.alkhawarizm.gguf.loader.GGUFParser;
import tech.kayys.alkhawarizm.gguf.loader.GGUFReader;
import tech.kayys.alkhawarizm.gguf.runtime.GgufBudget;
import tech.kayys.alkhawarizm.gguf.runtime.GgufRuntimeProbe;
import tech.kayys.alkhawarizm.gguf.runtime.GgufRuntimeProfile;
import tech.kayys.alkhawarizm.gguf.runtime.GgufTensorOps;
import tech.kayys.gollek.plugin.runner.RunnerRequest;
import tech.kayys.gollek.plugin.runner.RunnerResult;

import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Java-native GGUF engine.
 *
 * <p>This is the same class that previously lived in
 * {@code alkhawarizm-gguf-java}'s runner package, moved here unchanged in
 * behavior: it loads the model and reports a rich readiness profile, but
 * still refuses to generate. That refusal is intentional (see
 * {@link JavaNativeGgufBackendProvider}) — it stays in place until the
 * decoder loop in {@code alkhawarizm-gguf-core} (tokenizer, transformer
 * forward pass, sampler, KV cache) is wired up end to end and verified
 * against llama.cpp golden output for a given architecture.
 *
 * <p>Wiring that loop in is the actual next step, not a rewrite of this
 * class: {@code GGUFModel}/{@code GgufRuntimeProfile} (loading + readiness),
 * {@code LlamaModel}/{@code LlamaDecoderLayer}/{@code LlamaAttention}/
 * {@code LlamaMLP} (forward pass), the quantized dot-product kernels under
 * {@code gguf.runtime}, {@code GGUFTokenizer}/{@code BPETokenizer}, and
 * {@code Sampler} already exist in {@code alkhawarizm-gguf-core} — {@link #execute}
 * below just needs to call them in sequence instead of returning failure.</p>
 */
final class JavaNativeGgufBackend implements GgufBackend {
    private final GGUFModel model;
    private final GgufRuntimeProfile profile;
    private final GgufRuntimeProbe.PreparedMatrixCacheDecision preparedCacheDecision;

    JavaNativeGgufBackend(Path modelPath) throws Exception {
        long startNanos = System.nanoTime();
        this.model = loadModel(modelPath);
        long loadMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
        this.profile = GgufRuntimeProfile.fromModel(model, Files.size(modelPath), loadMillis);
        this.preparedCacheDecision = prepareDecoderMatrixCaches(model);
    }

    @Override
    public String name() {
        return "java";
    }

    @Override
    public Map<String, Object> metadata() {
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("backend", name());
        metadata.put("javaStatus", profile.javaStatus());
        metadata.put("architecture", profile.architecture());
        metadata.put("ggufVersion", profile.ggufVersion());
        metadata.put("tensorCount", profile.tensorCount());
        metadata.put("decoderTensorRatio", profile.decoderTensorRatio());
        metadata.put("missingDecoderTensorCount", profile.missingDecoderTensorCount());
        metadata.put("malformedDecoderTensorCount", profile.malformedDecoderTensorCount());
        metadata.put("missingDecoderTensorExamples", profile.missingDecoderTensorExamples());
        metadata.put("malformedDecoderTensorExamples", profile.malformedDecoderTensorExamples());
        metadata.put("knownTensorTypeRatio", profile.knownTensorTypeRatio());
        metadata.put("preparedMatrixCachePlan", preparedCacheDecision.plan().compactSummary());
        metadata.put("preparedMatrixCache", preparedCacheDecision.compactSummary());
        metadata.put("preparedMatrixCacheMode", preparedCacheDecision.mode());
        metadata.put("preparedMatrixCacheEstimatedBytes", preparedCacheDecision.plan().estimatedPreparedBytes());
        metadata.put("preparedMatrixCacheBytes", preparedCacheDecision.stats().cacheBytes());
        metadata.put("preparedMatrixCacheEntries", preparedCacheDecision.stats().cacheEntries());
        metadata.put("loaderReady", profile.knownTensorTypeRatio() > 0.0d);
        metadata.put("decoderTensorsReady", profile.decoderTensorSetComplete());
        metadata.put("rowDotReady", profile.rowDotPrimitivesReady());
        metadata.put("generationReady", false);
        if (profile.modelConfig() != null) {
            metadata.put("modelType", profile.modelConfig().modelType());
            metadata.put("layers", profile.modelConfig().numHiddenLayers());
            metadata.put("hiddenSize", profile.modelConfig().hiddenSize());
            metadata.put("attentionHeads", profile.modelConfig().numAttentionHeads());
            metadata.put("kvHeads", profile.modelConfig().resolvedNumKvHeads());
            metadata.put("headDim", profile.modelConfig().resolvedHeadDim());
            metadata.put("contextLength", profile.modelConfig().maxPositionEmbeddings());
            metadata.put("vocabSize", profile.modelConfig().vocabSize());
        }
        return Map.copyOf(metadata);
    }

    @Override
    public <T> RunnerResult<T> execute(RunnerRequest request) {
        // TODO(gguf-generation): tokenize request -> LlamaModel.forward() per
        // step -> Sampler.sample() -> detokenize -> stream/return. Gate the
        // architecture in JavaNativeGgufBackendProvider.ARCHITECTURES_READY_FOR_GENERATION
        // once this path is verified against llama.cpp golden output.
        return RunnerResult.failed(
                "Java-native GGUF generation is not enabled yet (" + profile.javaStatus()
                        + "; preparedMatrixCache=" + preparedCacheDecision.compactSummary()
                        + "). Use gguf.backend=llamacpp, or add alkhawarizm-gguf-llamacpp to the classpath.");
    }

    @Override
    public void close() {
        GgufTensorOps.clearPreparedMatrixCaches(model);
        model.close();
    }

    private static GgufRuntimeProbe.PreparedMatrixCacheDecision prepareDecoderMatrixCaches(GGUFModel model) {
        int explicitMinRows = Math.max(0, Integer.getInteger("alkhawarizm.gguf.java_native.prepare_min_rows", 0));
        if (explicitMinRows > 0) {
            return GgufRuntimeProbe.prepareDecoderMatrixCaches(
                    model,
                    GgufRuntimeProbe.selectDecoderPreparedMatrixCache(model, explicitMinRows, false, 1, 0L));
        }

        int autoMinRows = Math.max(1, Integer.getInteger("alkhawarizm.gguf.java_native.auto_prepare_min_rows", 32));
        long budgetBytes = GgufBudget.byteSizeProperty(
                "alkhawarizm.gguf.java_native.auto_prepare_budget_bytes", GgufBudget.defaultAutoPrepareBytes());
        return GgufRuntimeProbe.prepareDecoderMatrixCaches(
                model,
                GgufRuntimeProbe.selectDecoderPreparedMatrixCache(
                        model,
                        0,
                        Boolean.parseBoolean(System.getProperty("alkhawarizm.gguf.java_native.auto_prepare", "true")),
                        autoMinRows,
                        budgetBytes));
    }

    private static GGUFModel loadModel(Path modelPath) throws Exception {
        Arena arena = Arena.ofShared();
        try (GGUFReader reader = new GGUFReader(modelPath, arena)) {
            return new GGUFParser().parse(reader.segment(), arena);
        } catch (Exception exception) {
            arena.close();
            throw exception;
        } catch (Error error) {
            arena.close();
            throw error;
        }
    }
}
