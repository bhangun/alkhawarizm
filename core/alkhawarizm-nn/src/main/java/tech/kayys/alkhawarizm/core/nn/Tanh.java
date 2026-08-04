package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

/**
 * Tanh activation layer.
 */
public class Tanh extends Module {

    @Override
    public Tensor forward(Tensor input) {
        return input.tanh();
    }

    @Override
    public String toString() {
        return "Tanh()";
    }
}
