package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class GroupNorm extends NNModule {
    public GroupNorm(int numGroups, long numChannels) {}
    
    @Override
    public Tensor forward(Tensor input) {
        return input; // placeholder
    }
}
