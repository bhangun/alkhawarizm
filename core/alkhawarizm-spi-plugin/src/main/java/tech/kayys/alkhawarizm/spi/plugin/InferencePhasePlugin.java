package tech.kayys.alkhawarizm.spi.plugin;

import tech.kayys.alkhawarizm.spi.context.EngineContext;
import tech.kayys.alkhawarizm.spi.execution.ExecutionContext;
import tech.kayys.alkhawarizm.spi.exception.PluginException;
import tech.kayys.alkhawarizm.spi.inference.InferencePhase;

/**
 * Plugin that executes during a specific inference phase.
 * This is the primary extension point for custom logic.
 */
public interface InferencePhasePlugin extends AlkhawarizmPlugin {

    /**
     * Get the execution order within the phase.
     * Lower values execute first.
     */
    default int order() {
        return 100;
    }

    /**
     * The phase this plugin is bound to
     */
    InferencePhase phase();

    /**
     * Execute plugin logic for the current inference request.
     *
     * @param context Execution context with mutable state
     * @param engine  Global engine context (read-only)
     * @throws PluginException if plugin execution fails
     */
    void execute(ExecutionContext context, EngineContext engine)
            throws PluginException;

    /**
     * Check if plugin should execute for this context.
     * Allows conditional execution based on request metadata.
     *
     * Default: always execute
     */
    default boolean shouldExecute(ExecutionContext context) {
        return true;
    }

}
