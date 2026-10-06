package tech.kayys.alkhawarizm.gguf.llamacppbackend;

import org.jboss.logging.Logger;
import tech.kayys.alkhawarizm.gguf.api.GgufBackend;
import tech.kayys.alkhawarizm.gguf.llamacpp.LlamaCppRunner;
import tech.kayys.gollek.plugin.runner.RunnerRequest;
import tech.kayys.gollek.plugin.runner.RunnerResult;
import tech.kayys.gollek.spi.inference.InferenceRequest;
import tech.kayys.gollek.spi.inference.InferenceResponse;

/**
 * GGUF backend powered by llama.cpp native bindings.
 *
 * <p>Delegates to {@link LlamaCppRunner}, which owns the FFM bindings into
 * {@code libllama.dylib / libllama.so}. The runner is fully initialised by
 * {@link LlamaCppGgufBackendProvider} before this backend is returned.</p>
 */
final class LlamaCppGgufBackend implements GgufBackend {

    private static final Logger log = Logger.getLogger(LlamaCppGgufBackend.class);

    private final LlamaCppRunner runner;

    LlamaCppGgufBackend(LlamaCppRunner runner) {
        this.runner = runner;
    }

    @Override
    public String name() {
        return "llamacpp";
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> RunnerResult<T> execute(RunnerRequest request) {
        if (request.getInferenceRequest().isEmpty()) {
            return RunnerResult.failed("Unsupported request type for llama.cpp GGUF backend");
        }
        InferenceRequest inferenceRequest = request.getInferenceRequest().get();
        try {
            InferenceResponse response = runner.infer(inferenceRequest);
            return (RunnerResult<T>) RunnerResult.success(response);
        } catch (Exception e) {
            log.errorf(e, "Llama.cpp GGUF inference failed");
            return RunnerResult.failed("Llama.cpp GGUF inference failed: " + e.getMessage());
        }
    }

    @Override
    public void close() {
        try {
            runner.close();
        } catch (Exception e) {
            log.warnf(e, "Error closing LlamaCppRunner");
        }
    }
}
