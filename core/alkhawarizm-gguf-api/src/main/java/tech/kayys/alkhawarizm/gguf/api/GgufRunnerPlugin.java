package tech.kayys.alkhawarizm.gguf.api;

import tech.kayys.gollek.plugin.runner.ModelHandle;
import tech.kayys.gollek.plugin.runner.ModelLoadException;
import tech.kayys.gollek.plugin.runner.ModelLoadRequest;
import tech.kayys.gollek.plugin.runner.RunnerContext;
import tech.kayys.gollek.plugin.runner.RunnerException;
import tech.kayys.gollek.plugin.runner.RunnerInitializationException;
import tech.kayys.gollek.plugin.runner.RunnerPlugin;
import tech.kayys.gollek.plugin.runner.RunnerRequest;
import tech.kayys.gollek.plugin.runner.RunnerResult;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.ServiceLoader;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * ServiceLoader-based GGUF runner plugin — v3.0.0.
 *
 * <p>Discovers backends via {@link java.util.ServiceLoader} so that:</p>
 * <ul>
 *   <li>this module has no compile-time dependency on any backend;</li>
 *   <li>"pure Java" is a structural property (omit the llama.cpp jar) rather
 *       than a runtime default;</li>
 *   <li>new backends need only a {@code META-INF/services} registration.</li>
 * </ul>
 *
 * <p>AUTO selection:</p>
 * <ol>
 *   <li>Sort available providers by descending {@link GgufBackendProvider#priority}.</li>
 *   <li>Probe each; take the first that reports
 *       {@link GgufBackendCapability#generationReady()} for this model.</li>
 *   <li>If none is ready, fall back to the highest-priority available provider
 *       and let its own {@code execute()} report a specific reason — better than
 *       a generic "no backend available" here.</li>
 * </ol>
 */
public final class GgufRunnerPlugin implements RunnerPlugin {
    public static final String ID = "gguf-runner";

    private final Map<String, GgufBackend> backends = new ConcurrentHashMap<>();
    private final List<GgufBackendProvider> providers;
    private volatile boolean initialized;

    public GgufRunnerPlugin() {
        this(loadProviders());
    }

    /** Visible for testing: inject providers directly instead of via ServiceLoader. */
    GgufRunnerPlugin(List<GgufBackendProvider> providers) {
        List<GgufBackendProvider> sorted = new ArrayList<>(providers);
        sorted.sort(Comparator.comparingInt(GgufBackendProvider::priority).reversed());
        this.providers = List.copyOf(sorted);
    }

    private static List<GgufBackendProvider> loadProviders() {
        List<GgufBackendProvider> found = new ArrayList<>();
        ServiceLoader.load(GgufBackendProvider.class, GgufRunnerPlugin.class.getClassLoader())
                .forEach(found::add);
        return found;
    }

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String name() {
        return "GGUF Unified Runner";
    }

    @Override
    public String version() {
        return "3.0.0";
    }

    @Override
    public String description() {
        return "GGUF model support with pluggable backends (java, llamacpp, ...) "
                + "discovered at runtime; ships zero, one, or many depending on which "
                + "backend jars are on the classpath.";
    }

    @Override
    public String format() {
        return "gguf";
    }

    @Override
    public Set<String> supportedFormats() {
        return Set.of(".gguf");
    }

    @Override
    public Set<String> supportedArchitectures() {
        return Set.of("llama", "mistral", "mixtral", "phi", "gemma", "qwen");
    }

    @Override
    public void initialize(RunnerContext context) {
        initialized = true;
    }

    @Override
    public boolean isAvailable() {
        return providers.stream().anyMatch(GgufBackendProvider::isAvailable);
    }

    @Override
    public ModelHandle loadModel(ModelLoadRequest request, RunnerContext context) throws RunnerException {
        ensureInitialized();
        Path modelPath = Path.of(request.getModelPath());
        GgufBackendSelection selection = GgufBackendSelection.resolve(request, context);

        GgufBackendProvider provider = selectProvider(modelPath, selection);
        try {
            GgufBackend backend = provider.create(modelPath, context);
            Map<String, Object> metadata = new LinkedHashMap<>(backend.metadata());
            metadata.put("backendProvider", provider.id());
            metadata.put("backendSelection", selection.normalizedValue());
            metadata.put("backendSelectionSource", selection.source());
            metadata.put("backendSelectionExplicit", selection.explicit());

            ModelHandle handle = ModelHandle.of(request.getModelPath(), "gguf", Map.copyOf(metadata));
            backends.put(handle.getModelId(), backend);
            return handle;
        } catch (RunnerException e) {
            throw e;
        } catch (Exception e) {
            throw new ModelLoadException(request.getModelPath(), "Failed to load GGUF model: " + e.getMessage(), e);
        }
    }

    @Override
    public void unloadModel(ModelHandle handle, RunnerContext context) {
        Optional.ofNullable(backends.remove(handle.getModelId())).ifPresent(GgufBackend::close);
    }

    @Override
    public <T> RunnerResult<T> execute(RunnerRequest request, RunnerContext context) throws RunnerException {
        ensureInitialized();
        Optional<GgufBackend> backend = backendForRequest(request);
        if (backend.isEmpty()) {
            return RunnerResult.failed("No GGUF model loaded for request. Pass modelId or load exactly one model.");
        }
        return backend.get().execute(request);
    }

    @Override
    public void shutdown() {
        backends.values().forEach(GgufBackend::close);
        backends.clear();
        initialized = false;
    }

    private void ensureInitialized() throws RunnerException {
        if (!initialized) {
            throw new RunnerInitializationException("gguf", "Plugin not initialized");
        }
        if (providers.isEmpty()) {
            throw new RunnerInitializationException("gguf",
                    "No GgufBackendProvider found on the classpath. Add alkhawarizm-gguf-java "
                            + "and/or alkhawarizm-gguf-llamacpp as a dependency.");
        }
    }

    private GgufBackendProvider selectProvider(Path modelPath, GgufBackendSelection selection)
            throws RunnerException {
        if (selection.explicit()) {
            return providers.stream()
                    .filter(p -> matches(p, selection.normalizedValue()))
                    .filter(GgufBackendProvider::isAvailable)
                    .findFirst()
                    .orElseThrow(() -> new RunnerException(
                            "Requested GGUF backend '" + selection.requestedValue() + "' is not available. "
                                    + "Available backends: " + availableIds()));
        }

        // AUTO: ask each available provider, in priority order, whether it can
        // actually generate for this model.
        for (GgufBackendProvider provider : providers) {
            if (!provider.isAvailable()) {
                continue;
            }
            try {
                GgufBackendCapability capability = provider.probe(modelPath);
                if (capability.generationReady()) {
                    return provider;
                }
            } catch (Exception ignored) {
                // A failed probe just disqualifies this provider for AUTO; an
                // explicit request for it would still surface the real error.
            }
        }

        // Nothing claims readiness — fall back to the highest-priority available
        // provider so its own execute() can report a specific "not ready" reason
        // instead of a generic "no backend available" message here.
        return providers.stream()
                .filter(GgufBackendProvider::isAvailable)
                .findFirst()
                .orElseThrow(() -> new RunnerException(
                        "No GGUF backend providers are available on the classpath."));
    }

    private static boolean matches(GgufBackendProvider provider, String normalizedValue) {
        return provider.id().equalsIgnoreCase(normalizedValue) || provider.aliases().contains(normalizedValue);
    }

    private String availableIds() {
        return providers.stream()
                .filter(GgufBackendProvider::isAvailable)
                .map(GgufBackendProvider::id)
                .collect(Collectors.joining(", "));
    }

    private Optional<GgufBackend> backendForRequest(RunnerRequest request) {
        Optional<String> requestedModel = request.getParameter("modelId", String.class)
                .or(() -> valueAsString(request.metadata().get("modelId")))
                .or(() -> valueAsString(request.metadata().get("model.id")));
        if (requestedModel.isPresent()) {
            return Optional.ofNullable(backends.get(requestedModel.get()));
        }
        if (backends.size() == 1) {
            return backends.values().stream().findFirst();
        }
        return Optional.empty();
    }

    private static Optional<String> valueAsString(Object value) {
        return value == null ? Optional.empty() : Optional.of(value.toString());
    }
}
