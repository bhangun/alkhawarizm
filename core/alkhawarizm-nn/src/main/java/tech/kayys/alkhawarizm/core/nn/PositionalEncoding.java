package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class PositionalEncoding extends NNModule {
    public PositionalEncoding(int dModel, int maxLen) {}
    
    @Override
    public Tensor forward(Tensor input) {
        return input; // placeholder
    }
}
