package tech.kayys.alkhawarizm.gguf.llamacppbackend;

import tech.kayys.alkhawarizm.gguf.api.GgufBackend;
import tech.kayys.alkhawarizm.gguf.api.GgufBackendCapability;
import tech.kayys.alkhawarizm.gguf.api.GgufBackendProvider;
import tech.kayys.gollek.plugin.runner.RunnerContext;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Registers the llama.cpp bridge with {@code GgufRunnerPlugin} via
 * {@link java.util.ServiceLoader}. This is the entire compile-time surface
 * between the runner and llama.cpp: an assembly that omits this module's jar
 * has no llama.cpp code on its classpath at all, which is what makes "pure
 * Java" an enforceable build configuration rather than a runtime default.
 *
 * <p>CDI resolution uses reflection so that this module does not carry a
 * compile-time dependency on any llama.cpp-specific inference class. The
 * class name {@code tech.kayys.alkhawarizm.inference.llamacpp.LlamaCppProvider}
 * is looked up at runtime; if CDI or the class is absent the provider simply
 * reports itself as unavailable.</p>
 */
public final class LlamaCppGgufBackendProvider implements GgufBackendProvider {

    private static final String LLAMACPP_PROVIDER_CLASS =
            "tech.kayys.alkhawarizm.inference.llamacpp.LlamaCppProvider";

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
        return resolveProvider().isPresent();
    }

    @Override
    public GgufBackendCapability probe(Path modelPath) {
        // llama.cpp covers essentially every architecture/quant format it ships
        // GGML kernels for; treat "the provider resolves" as "ready" and let
        // create() surface any actual model-specific load failure.
        return GgufBackendCapability.ready(null, Set.of(), Map.of());
    }

    @Override
    public GgufBackend create(Path modelPath, RunnerContext context) throws Exception {
        Object provider = resolveProvider()
                .orElseThrow(() -> new IllegalStateException(
                        "LlamaCppProvider is not available in this deployment. "
                                + "Ensure the llama.cpp integration module is on the classpath and CDI is active."));
        return new LlamaCppGgufBackend(provider);
    }

    @Override
    public int priority() {
        // Below the Java engine's priority (10): once Java reports itself
        // ready for an architecture, AUTO prefers it. llama.cpp remains the
        // catch-all for everything Java doesn't support yet.
        return -10;
    }

    private static Optional<Object> resolveProvider() {
        try {
            Class<?> providerClass = Class.forName(LLAMACPP_PROVIDER_CLASS);
            Class<?> cdiClass = Class.forName("jakarta.enterprise.inject.spi.CDI");
            Object cdi = cdiClass.getMethod("current").invoke(null);
            Method select = cdiClass.getMethod("select", Class.class, Annotation[].class);
            Object instance = select.invoke(cdi, providerClass, new Annotation[0]);
            Object provider = instance.getClass().getMethod("get").invoke(instance);
            return Optional.ofNullable(provider);
        } catch (ReflectiveOperationException | LinkageError | IllegalStateException ignored) {
            return Optional.empty();
        }
    }
}
