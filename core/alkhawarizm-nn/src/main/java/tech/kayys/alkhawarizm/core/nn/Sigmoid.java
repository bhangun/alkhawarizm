package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

/**
 * Sigmoid activation layer.
 */
public class Sigmoid extends NNModule {

    @Override
    public Tensor forward(Tensor input) {
        return input.sigmoid();
    }

    @Override
    public String toString() {
        return "Sigmoid()";
    }
}
