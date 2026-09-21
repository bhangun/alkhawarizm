package tech.kayys.alkhawarizm.gguf.javabackend;

import org.junit.jupiter.api.Test;
import tech.kayys.alkhawarizm.gguf.api.GgufBackendSelection;
import tech.kayys.gollek.plugin.runner.ModelLoadRequest;
import tech.kayys.gollek.plugin.runner.RunnerContext;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GgufBackendSelectionTest {

    @Test
    void defaultsToAutoWhenNoBackendRequested() {
        GgufBackendSelection selection = resolve(Map.of(), RunnerContext.empty());

        assertEquals("auto", selection.normalizedValue());
        assertFalse(selection.explicit());
    }

    @Test
    void allowsExplicitJavaBackendByAlias() {
        GgufBackendSelection selection = resolve(Map.of("gguf.backend", "java-native"), RunnerContext.empty());

        assertEquals("java-native", selection.normalizedValue());
        assertTrue(selection.explicit());
    }

    @Test
    void allowsExplicitLlamaCppBackendByAlias() {
        GgufBackendSelection selection = resolve(Map.of("gguf.backend", "llama.cpp"), RunnerContext.empty());

        assertEquals("llama.cpp", selection.normalizedValue());
        assertTrue(selection.explicit());
    }

    @Test
    void readsContextParameterForBackendSelection() {
        RunnerContext context = RunnerContext.withParameters(Map.of("gguf.backend", "llamacpp"));
        GgufBackendSelection selection = resolve(Map.of(), context);

        assertEquals("llamacpp", selection.normalizedValue());
        assertEquals("context.parameter.gguf.backend", selection.source());
        assertTrue(selection.explicit());
    }

    @Test
    void unknownBackendTokenBecomesExplicitRequest() {
        // Unknown tokens are passed through as explicit — GgufRunnerPlugin will
        // raise a clear error listing available backends rather than silently
        // falling back.
        GgufBackendSelection selection = resolve(Map.of("gguf.backend", "fastest-please"), RunnerContext.empty());

        assertEquals("fastest-please", selection.normalizedValue());
        assertEquals("fastest-please", selection.requestedValue());
        assertTrue(selection.explicit());
    }

    @Test
    void autoValueNormalizesToAutoToken() {
        GgufBackendSelection selection = resolve(Map.of("gguf.backend", "auto"), RunnerContext.empty());

        assertEquals("auto", selection.normalizedValue());
        assertFalse(selection.explicit());
    }

    private static GgufBackendSelection resolve(Map<String, Object> metadata, RunnerContext context) {
        ModelLoadRequest request = ModelLoadRequest.builder()
                .modelPath("/tmp/model.gguf")
                .metadata(metadata)
                .build();
        return GgufBackendSelection.resolve(request, context);
    }
}
