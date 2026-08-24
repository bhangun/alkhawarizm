package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class LeakyReLU extends NNModule {
    public LeakyReLU(float negativeSlope) {}
    
    @Override
    public Tensor forward(Tensor input) {
        return input; // placeholder
    }
}
