package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

/**
 * ReLU activation layer.
 * @author bhangun
 */
public class ReLU extends NNModule {

    private final boolean inplace;

    public ReLU(boolean inplace) {
        this.inplace = inplace;
    }

    public ReLU() {
        this(false);
    }

    @Override
    public Tensor forward(Tensor input) {
        return input.relu();
    }

    @Override
    public String toString() {
        return "ReLU(inplace=" + inplace + ")";
    }
}
