package tech.kayys.alkhawarizm.spi.model;

import tech.kayys.alkhawarizm.spi.spec.*;
import tech.kayys.alkhawarizm.core.tensor.DeviceType;
import tech.kayys.alkhawarizm.core.model.ModelFormat;

import java.util.Map;
/**
 * Immutable record representing modelref data.
 *
 * @author bhangun
 * @since 0.1.0
 */


public record ModelRef(
                String scheme, // hf, local, s3, git, http, custom
                String namespace, // org/user
                String name,
                String version,
                Map<String, String> parameters) {
}
