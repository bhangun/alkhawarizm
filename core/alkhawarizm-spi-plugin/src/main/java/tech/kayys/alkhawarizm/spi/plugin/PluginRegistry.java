package tech.kayys.alkhawarizm.spi.plugin;

import java.util.List;
import java.util.Optional;

/**
 * Registry for managing plugin lifecycle and discovery.
 */
public interface PluginRegistry {
    void initialize();

    void registerPlugin(AlkhawarizmPlugin plugin);

    void unregisterPlugin(String pluginId);

    List<AlkhawarizmPlugin> all();

    <T extends AlkhawarizmPlugin> List<T> byType(Class<T> type);

    Optional<AlkhawarizmPlugin> byId(String pluginId);

    void reload(String pluginId);

    List<AlkhawarizmPlugin.PluginMetadata> listMetadata();

    boolean isHealthy();

    List<String> unhealthyPlugins();

    void shutdownAll();
}
