package tech.kayys.alkhawarizm.gguf.api;

import tech.kayys.gollek.plugin.runner.RunnerContext;

import java.nio.file.Path;
import java.util.Set;

/**
 * SPI for a GGUF execution backend.
 *
 * <p>Implementations are discovered at runtime via {@link java.util.ServiceLoader}:
 * a backend module registers itself with a
 * {@code META-INF/services/tech.kayys.alkhawarizm.gguf.api.GgufBackendProvider}
 * file listing its provider class. Neither this module nor
 * {@link GgufRunnerPlugin} references any specific backend implementation
 * class, so:</p>
 *
 * <ul>
 *   <li>the Java engine and the llama.cpp bridge live in independent Maven
 *       modules with no compile-time dependency on each other;</li>
 *   <li>"pure Java" becomes a real, enforceable deployment: an assembly that
 *       excludes the llama.cpp jar structurally cannot select it, rather
 *       than merely defaulting away from it;</li>
 *   <li>adding a third backend later (e.g. a GPU engine) needs no change to
 *       this module or to {@link GgufRunnerPlugin}.</li>
 * </ul>
 */
public interface GgufBackendProvider {

    /** Stable id used for explicit selection, e.g. {@code "java"} or {@code "llamacpp"}. */
    String id();

    /** Other tokens (case-insensitive) that should resolve to this provider. */
    default Set<String> aliases() {
        return Set.of();
    }

    /**
     * Whether this backend can run at all in this JVM/deployment — e.g. the
     * llama.cpp provider checks that its native bridge actually resolves.
     * Called at startup and is expected to be cheap and side-effect-free.
     */
    boolean isAvailable();

    /**
     * Cheap, header-only readiness check for one specific model file. Must
     * not fully load tensor data. Called for every available provider on
     * every AUTO-mode model load, so keep it fast.
     */
    GgufBackendCapability probe(Path modelPath) throws Exception;

    /** Fully instantiate a backend for this model. May be expensive (full load). */
    GgufBackend create(Path modelPath, RunnerContext context) throws Exception;

    /**
     * Tie-breaker when more than one available provider reports
     * {@code generationReady} for the same model in AUTO mode. Higher wins.
     * The Java engine should outrank llama.cpp once it is genuinely ready
     * for a given architecture, so AUTO prefers the pure-Java path whenever
     * it can and only falls through to llama.cpp for what Java doesn't
     * support yet.
     */
    default int priority() {
        return 0;
    }
}
