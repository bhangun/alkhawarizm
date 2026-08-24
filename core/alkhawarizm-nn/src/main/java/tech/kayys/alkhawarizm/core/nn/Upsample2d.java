package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class Upsample2d extends NNModule {
    public Upsample2d(float scaleFactor) {}
    
    @Override
    public Tensor forward(Tensor input) {
        return input; // placeholder
    }
}
