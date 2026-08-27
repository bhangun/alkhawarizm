package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

/**
 * Tanh activation layer.
 * @author bhangun
 */
public class Tanh extends NNModule {

    @Override
    public Tensor forward(Tensor input) {
        return input.tanh();
    }

    @Override
    public String toString() {
        return "Tanh()";
    }
}
