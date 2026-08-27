package tech.kayys.alkhawarizm.spi.model;

import tech.kayys.alkhawarizm.spi.spec.*;
import tech.kayys.alkhawarizm.core.tensor.DeviceType;
import tech.kayys.alkhawarizm.core.model.ModelFormat;

/**
 * Supported FFN activation functions.
 * @author bhangun
 */
public enum FFNActivationType {
    SILU, // LLaMA, Mistral
    GELU, // Gemma, Gemma-2
    RELU,
    GELU_QUICK
}
