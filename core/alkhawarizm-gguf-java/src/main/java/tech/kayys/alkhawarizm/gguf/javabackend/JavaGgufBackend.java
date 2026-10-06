package tech.kayys.alkhawarizm.gguf.javabackend;

import tech.kayys.alkhawarizm.gguf.api.GgufBackend;
import tech.kayys.alkhawarizm.gguf.loader.GGUFModel;
import tech.kayys.alkhawarizm.gguf.loader.GGUFParser;
import tech.kayys.alkhawarizm.gguf.loader.GGUFReader;
import tech.kayys.alkhawarizm.gguf.loader.inference.KVCache;
import tech.kayys.alkhawarizm.gguf.loader.sampler.Sampler;
import tech.kayys.alkhawarizm.gguf.runtime.GgufBudget;
import tech.kayys.alkhawarizm.gguf.runtime.GgufRuntimeProbe;
import tech.kayys.alkhawarizm.gguf.runtime.GgufRuntimeProfile;
import tech.kayys.alkhawarizm.gguf.runtime.GgufTensorOps;
import tech.kayys.alkhawarizm.gguf.tokenizer.GGUFTokenizer;
import tech.kayys.gollek.models.core.ChatTemplateFormatter;
import tech.kayys.gollek.plugin.runner.RunnerRequest;
import tech.kayys.gollek.plugin.runner.RunnerResult;
import tech.kayys.gollek.spi.Message;
import tech.kayys.gollek.spi.inference.InferenceRequest;
import tech.kayys.gollek.spi.inference.InferenceResponse;
import tech.kayys.gollek.tokenizer.spi.DecodeOptions;
import tech.kayys.gollek.tokenizer.spi.EncodeOptions;

import java.lang.foreign.Arena;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Pure-Java GGUF engine.
 *
 * <p><b>Status: real generation loop, functionally unverified.</b> Runs
 * tokenizer -> decoder -> sampler -> KV cache using {@link GgufDecodeStep}
 * and {@link GgufModelConfig}. It has NOT been run against a real model or
 * compared to llama.cpp output —
 * {@link JavaGgufBackendProvider#ARCHITECTURES_READY_FOR_GENERATION}
 * stays empty until that happens. An explicit {@code gguf.backend=java}
 * request still routes here for testing in the meantime.</p>
 *
 * <p><b>Response shape parity:</b> builds a real {@link InferenceResponse} —
 * {@code requestId}/{@code model}/{@code sessionId} from the request when
 * present, {@code finishReason} computed correctly (STOP when the loop
 * broke on an EOS token, LENGTH when it hit {@code maxTokens} or the
 * context limit first) — closing the response-shape gap against the
 * llama.cpp backend.</p>
 */
final class JavaGgufBackend implements GgufBackend {
    private final GGUFModel model;
    private final GgufRuntimeProfile profile;
    private final GgufRuntimeProbe.PreparedMatrixCacheDecision preparedCacheDecision;
    private final GgufModelConfig cfg;
    private final GgufDecodeStep decodeStep;
    private final GGUFTokenizer tokenizer;

    JavaGgufBackend(Path modelPath) throws Exception {
        long startNanos = System.nanoTime();
        this.model = loadModel(modelPath);
        long loadMillis = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();
        this.profile = GgufRuntimeProfile.fromModel(model, Files.size(modelPath), loadMillis);
        this.preparedCacheDecision = prepareDecoderMatrixCaches(model);
        this.cfg = GgufModelConfig.from(model);
        this.decodeStep = new GgufDecodeStep(model, cfg);
        this.tokenizer = new GGUFTokenizer(model);
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
        metadata.put("rolloutStage", GgufRolloutStage.forArchitecture(
                profile.architecture(), JavaGgufBackendProvider.ARCHITECTURES_READY_FOR_GENERATION).name());
        metadata.put("chatTemplateSupported", ChatTemplateFormatter.supportsModelType(cfg.architecture()));
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
    @SuppressWarnings("unchecked")
    public <T> RunnerResult<T> execute(RunnerRequest request) {
        try {
            String prompt = extractPrompt(request);
            if (prompt.isBlank()) {
                return RunnerResult.failed("No prompt or inference request messages provided");
            }

            SamplingParams sampling = SamplingParams.from(request);

            EncodeOptions encodeOptions = EncodeOptions.builder()
                    .addBos(tokenizer.shouldAddBos())
                    .addEos(tokenizer.shouldAddEos())
                    .build();
            long[] promptTokens = tokenizer.encode(prompt, encodeOptions);
            if (promptTokens.length == 0) {
                return RunnerResult.failed("Prompt tokenized to zero tokens.");
            }
            if (promptTokens.length >= cfg.contextLength()) {
                return RunnerResult.failed("Prompt (" + promptTokens.length
                        + " tokens) does not fit in context length " + cfg.contextLength());
            }

            long startNanos = System.nanoTime();
            KVCache cache = new KVCache(cfg.nLayers(), cfg.contextLength(), cfg.nKVHeads(), cfg.headDim());
            Sampler sampler = new Sampler(
                    sampling.temperature(), sampling.topK(), sampling.topP(), sampling.repeatPenalty());

            List<Integer> history = new ArrayList<>(); // prompt + generated, for repetition penalty
            List<Integer> generated = new ArrayList<>();

            int pos = 0;
            float[] logits = null;
            for (long tokenId : promptTokens) {
                logits = decodeStep.forward((int) tokenId, pos, cache);
                history.add((int) tokenId);
                pos++;
            }

            InferenceResponse.FinishReason finishReason = InferenceResponse.FinishReason.LENGTH;
            for (int i = 0; i < sampling.maxTokens() && pos < cfg.contextLength(); i++) {
                int nextToken = sampler.sample(logits, history);
                if (tokenizer.isEosToken(nextToken)) {
                    finishReason = InferenceResponse.FinishReason.STOP;
                    break;
                }
                generated.add(nextToken);
                history.add(nextToken);
                logits = decodeStep.forward(nextToken, pos, cache);
                pos++;
            }

            long[] generatedTokens = generated.stream().mapToLong(Integer::longValue).toArray();
            DecodeOptions decodeOptions = DecodeOptions.builder().build();
            String text = tokenizer.decode(generatedTokens, decodeOptions);
            long durationMs = Duration.ofNanos(System.nanoTime() - startNanos).toMillis();

            InferenceRequest inferenceRequest = request.getInferenceRequest().orElse(null);
            InferenceResponse response = new InferenceResponse(
                    inferenceRequest != null ? inferenceRequest.getRequestId() : java.util.UUID.randomUUID().toString(),
                    text,
                    inferenceRequest != null ? inferenceRequest.getModel() : null,
                    0, // tokensUsed: 0 triggers the constructor's own inputTokens+outputTokens fallback
                    promptTokens.length,
                    generated.size(),
                    durationMs,
                    null, // timestamp: constructor defaults to Instant.now()
                    Map.of("backend", "java"),
                    null, // toolCalls: Java engine doesn't support tool calls; constructor defaults to empty list
                    finishReason,
                    inferenceRequest != null ? inferenceRequest.getSessionId().orElse(null) : null);

            return (RunnerResult<T>) RunnerResult.success(response);
        } catch (Exception e) {
            return RunnerResult.failed("Java GGUF generation failed: " + e.getMessage());
        }
    }

    private String extractPrompt(RunnerRequest request) {
        Optional<String> explicit = request.getParameter("prompt", String.class);
        if (explicit.isPresent() && !explicit.get().isBlank()) {
            return explicit.get();
        }

        InferenceRequest req = request.getInferenceRequest()
                .orElseThrow(() -> new IllegalArgumentException("No prompt or inference request messages provided"));

        List<Message> messages = req.getMessages();
        if (messages != null && !messages.isEmpty() && ChatTemplateFormatter.supportsModelType(cfg.architecture())) {
            return ChatTemplateFormatter.format(messages, cfg.architecture());
        }

        String prompt = req.getPrompt();
        return prompt != null ? prompt : "";
    }

    @Override
    public void close() {
        GgufTensorOps.clearPreparedMatrixCaches(model);
        model.close();
    }

    private static GgufRuntimeProbe.PreparedMatrixCacheDecision prepareDecoderMatrixCaches(GGUFModel model) {
        int explicitMinRows = Math.max(0, Integer.getInteger("alkhawarizm.gguf.java.prepare_min_rows", 0));
        if (explicitMinRows > 0) {
            return GgufRuntimeProbe.prepareDecoderMatrixCaches(
                    model,
                    GgufRuntimeProbe.selectDecoderPreparedMatrixCache(model, explicitMinRows, false, 1, 0L));
        }

        int autoMinRows = Math.max(1, Integer.getInteger("alkhawarizm.gguf.java.auto_prepare_min_rows", 32));
        long budgetBytes = GgufBudget.byteSizeProperty(
                "alkhawarizm.gguf.java.auto_prepare_budget_bytes", GgufBudget.defaultAutoPrepareBytes());
        return GgufRuntimeProbe.prepareDecoderMatrixCaches(
                model,
                GgufRuntimeProbe.selectDecoderPreparedMatrixCache(
                        model,
                        0,
                        Boolean.parseBoolean(System.getProperty("alkhawarizm.gguf.java.auto_prepare", "true")),
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

    private record SamplingParams(float temperature, int topK, float topP, float repeatPenalty, int maxTokens) {
        static SamplingParams from(RunnerRequest request) {
            Optional<InferenceRequest> inference = request.getInferenceRequest();
            if (inference.isPresent()) {
                InferenceRequest req = inference.get();
                return new SamplingParams(
                        (float) req.getTemperature(),
                        req.getTopK(),
                        (float) req.getTopP(),
                        (float) req.getRepeatPenalty(),
                        req.getMaxTokens());
            }
            return new SamplingParams(
                    request.getParameter("temperature", Double.class).map(Double::floatValue).orElse(0.2f),
                    request.getParameter("topK", Integer.class).orElse(40),
                    request.getParameter("topP", Double.class).map(Double::floatValue).orElse(0.9f),
                    request.getParameter("repeatPenalty", Double.class).map(Double::floatValue).orElse(1.1f),
                    request.getParameter("maxTokens", Integer.class).orElse(256));
        }
    }
}
