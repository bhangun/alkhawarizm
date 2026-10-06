package tech.kayys.alkhawarizm.gguf.llamacppbackend;

import org.jboss.logging.Logger;
import tech.kayys.alkhawarizm.gguf.api.GgufBackend;
import tech.kayys.alkhawarizm.gguf.api.GgufBackendCapability;
import tech.kayys.alkhawarizm.gguf.api.GgufBackendProvider;
import tech.kayys.alkhawarizm.gguf.llamacpp.GGUFChatTemplateService;
import tech.kayys.alkhawarizm.gguf.llamacpp.LlamaCppBinding;
import tech.kayys.alkhawarizm.gguf.llamacpp.LlamaCppRunner;
import tech.kayys.gollek.plugin.runner.RunnerContext;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.Set;

/**
 * Registers the native llama.cpp backend with {@code GgufRunnerPlugin} via
 * {@link java.util.ServiceLoader}.
 *
 * <p>This provider directly instantiates {@link LlamaCppRunner} (which owns
 * all Panama FFM downcalls into {@code libllama}) rather than relying on CDI
 * reflection. The assembly that omits this module's jar has no llama.cpp code
 * on its classpath at all — making "pure Java" an enforceable build config.</p>
 *
 * <p>Availability is determined by whether the native library can be located
 * via {@link LlamaCppBinding#load()} — it probes the same discovery chain used
 * at runtime (env vars, java.library.path, Homebrew prefix, etc.).</p>
 */
public final class LlamaCppGgufBackendProvider implements GgufBackendProvider {

    private static final Logger log = Logger.getLogger(LlamaCppGgufBackendProvider.class);

    @Override
    public String id() {
        return "llamacpp";
    }

    @Override
    public Set<String> aliases() {
        return Set.of("llama.cpp", "llama-cpp", "llama_cpp", "native", "binding");
    }

    @Override
    public boolean isAvailable() {
        try {
            LlamaCppBinding.load();
            return true;
        } catch (Throwable t) {
            log.debugf("llama.cpp native library not available: %s", t.getMessage());
            return false;
        }
    }

    @Override
    public GgufBackendCapability probe(Path modelPath) {
        if (!Files.exists(modelPath)) {
            return GgufBackendCapability.notReady(null, Set.of(), "Model path does not exist: " + modelPath, Map.of());
        }
        // llama.cpp can handle any GGUF it has GGML kernels for.
        // Let the actual load surface any model-specific incompatibility.
        return GgufBackendCapability.ready(null, Set.of(), Map.of());
    }

    @Override
    public GgufBackend create(Path modelPath, RunnerContext context) throws Exception {
        LlamaCppBinding binding = LlamaCppBinding.load();
        GGUFChatTemplateService templateService = new GGUFChatTemplateService();
        LlamaCppRunner runner = new LlamaCppRunner(binding, null, templateService);
        // Model loading happens lazily in LlamaCppRunner.initialize()
        // via the Gollek orchestration layer (LlamaCppEngine) at inference time.
        return new LlamaCppGgufBackend(runner);
    }

    @Override
    public int priority() {
        // Below the Java engine's priority (10): once Java reports itself
        // ready for an architecture, AUTO prefers it. llama.cpp remains the
        // catch-all for everything Java doesn't support yet.
        return -10;
    }
}
