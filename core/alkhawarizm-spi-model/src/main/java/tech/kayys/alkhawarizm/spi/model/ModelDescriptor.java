package tech.kayys.alkhawarizm.spi.model;

import tech.kayys.alkhawarizm.spi.spec.*;
import tech.kayys.alkhawarizm.core.tensor.DeviceType;
import tech.kayys.alkhawarizm.core.model.ModelFormat;

import java.net.URI;
import java.util.Map;
/**
 * Immutable record representing modeldescriptor data.
 *
 * @author bhangun
 * @since 0.1.0
 */


public record ModelDescriptor(
                String id,
                String format, // gguf, onnx, safetensors, triton, etc
                URI source,
                Map<String, String> metadata) {
}
