package tech.kayys.alkhawarizm.gguf.api;

import tech.kayys.gollek.plugin.runner.ModelLoadRequest;
import tech.kayys.gollek.plugin.runner.RunnerContext;

import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Resolves which backend the caller asked for, purely as a normalized
 * string token — it does not know what backends exist. {@link GgufRunnerPlugin}
 * matches {@link #normalizedValue()} against each registered
 * {@link GgufBackendProvider}'s {@code id()}/{@code aliases()}.
 *
 * <p>Previous versions of this class hard-coded a closed {@code JAVA
 * / LLAMACPP} enum, which meant every new backend required editing this
 * class. That coupling is gone: this class only extracts and normalizes the
 * requested token; provider matching (including alias resolution) happens
 * in {@link GgufRunnerPlugin} against whatever providers are actually
 * registered.</p>
 */
public record GgufBackendSelection(
        String requestedValue,
        String normalizedValue,
        String source,
        boolean explicit) {

    private static final String DEFAULT_SOURCE = "default:auto";

    public static GgufBackendSelection resolve(ModelLoadRequest request, RunnerContext context) {
        Objects.requireNonNull(request, "request");
        Objects.requireNonNull(context, "context");

        Candidate candidate = firstCandidate(request.getMetadata(), context);
        if (candidate.value().isEmpty()) {
            return auto(DEFAULT_SOURCE, "");
        }

        String raw = candidate.value().get();
        String normalized = normalize(raw);
        if (normalized.isEmpty() || normalized.equals("auto") || normalized.equals("default")) {
            return auto(candidate.source(), raw);
        }
        return new GgufBackendSelection(raw, normalized, candidate.source(), true);
    }

    private static GgufBackendSelection auto(String source, String raw) {
        return new GgufBackendSelection(raw, "auto", source, false);
    }

    private static Candidate firstCandidate(Map<String, Object> requestMetadata, RunnerContext context) {
        Optional<String> requestPlugin = stringValue(requestMetadata.get("plugin"));
        if (requestPlugin.isPresent()) {
            return new Candidate(requestPlugin, "request.metadata.plugin");
        }

        Optional<String> requestBackend = stringValue(requestMetadata.get("gguf.backend"))
                .or(() -> stringValue(requestMetadata.get("backend")));
        if (requestBackend.isPresent()) {
            return new Candidate(requestBackend, "request.metadata.gguf.backend");
        }

        Optional<String> contextBackend = context.getMetadataValue("gguf.backend")
                .flatMap(GgufBackendSelection::stringValue)
                .or(() -> context.getMetadataValue("backend").flatMap(GgufBackendSelection::stringValue));
        if (contextBackend.isPresent()) {
            return new Candidate(contextBackend, "context.metadata.gguf.backend");
        }

        Optional<String> parameterBackend = context.getParameter("gguf.backend")
                .flatMap(GgufBackendSelection::stringValue)
                .or(() -> context.getParameter("backend").flatMap(GgufBackendSelection::stringValue));
        if (parameterBackend.isPresent()) {
            return new Candidate(parameterBackend, "context.parameter.gguf.backend");
        }

        return new Candidate(Optional.empty(), DEFAULT_SOURCE);
    }

    private static Optional<String> stringValue(Object value) {
        if (value == null) {
            return Optional.empty();
        }
        String s = value.toString().trim();
        return s.isEmpty() ? Optional.empty() : Optional.of(s);
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }

    private record Candidate(Optional<String> value, String source) {
    }
}
