package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

/**
 * GELU activation layer.
 * @author bhangun
 */
public class GELU extends NNModule {

    @Override
    public Tensor forward(Tensor input) {
        return input.gelu();
    }

    @Override
    public String toString() {
        return "GELU()";
    }
}
