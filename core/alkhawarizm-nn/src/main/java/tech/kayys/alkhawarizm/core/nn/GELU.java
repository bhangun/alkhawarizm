package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

/**
 * GELU activation layer.
 */
public class GELU extends Module {

    @Override
    public Tensor forward(Tensor input) {
        return input.gelu();
    }

    @Override
    public String toString() {
        return "GELU()";
    }
}
