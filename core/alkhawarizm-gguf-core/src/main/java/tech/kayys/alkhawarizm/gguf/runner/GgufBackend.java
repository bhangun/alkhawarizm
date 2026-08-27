package tech.kayys.alkhawarizm.gguf.runner;

import tech.kayys.gollek.plugin.runner.RunnerRequest;
import tech.kayys.gollek.plugin.runner.RunnerResult;
import tech.kayys.gollek.plugin.runner.RunnerSession;

/**
 * Backend interface for GGUF execution.
 * @author bhangun
 */
public interface GgufBackend {
    <T> RunnerResult<T> execute(RunnerRequest request);
    void close();
}
