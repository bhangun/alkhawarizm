package tech.kayys.alkhawarizm.spi.model;

import tech.kayys.alkhawarizm.spi.spec.*;
import tech.kayys.alkhawarizm.core.tensor.DeviceType;
import tech.kayys.alkhawarizm.core.model.ModelFormat;

import java.nio.file.Path;
import java.util.Map;
/**
 * Immutable record representing modelartifact data.
 *
 * @author bhangun
 * @since 0.1.0
 */


public record ModelArtifact(
                Path path,
                String checksum,
                Map<String, String> metadata) {
}
