package tech.kayys.alkhawarizm.gguf.api;

import tech.kayys.gollek.plugin.runner.RunnerRequest;
import tech.kayys.gollek.plugin.runner.RunnerResult;

import java.util.Map;

/**
 * Execution contract that every GGUF backend must honour.
 *
 * <p>Backends are stateful (they hold file handles / native memory): they are
 * created once per {@code loadModel} call and released via {@link #close} when
 * the model is unloaded.</p>
 */
public interface GgufBackend extends AutoCloseable {

    /** Short stable identifier, e.g. {@code "java"} or {@code "llamacpp"}. */
    String name();

    /** Diagnostic key/value pairs surfaced in {@link tech.kayys.gollek.plugin.runner.ModelHandle} metadata. */
    default Map<String, Object> metadata() {
        return Map.of("backend", name());
    }

    <T> RunnerResult<T> execute(RunnerRequest request);

    @Override
    void close();
}
