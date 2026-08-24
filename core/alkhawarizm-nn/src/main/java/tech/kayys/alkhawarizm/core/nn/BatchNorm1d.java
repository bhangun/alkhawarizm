package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class BatchNorm1d extends NNModule {
    public BatchNorm1d(long numFeatures) {}
    
    @Override
    public Tensor forward(Tensor input) {
        return input; // placeholder
    }
}
