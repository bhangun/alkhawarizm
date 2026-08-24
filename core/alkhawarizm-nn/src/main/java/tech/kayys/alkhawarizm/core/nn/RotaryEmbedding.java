package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class RotaryEmbedding extends NNModule {
    public RotaryEmbedding(int headDim, int maxSeqLen) {}
    
    @Override
    public Tensor forward(Tensor input) {
        return input; // placeholder
    }
    
    public Tensor apply(Tensor q, Tensor k) {
        return q; // placeholder
    }
}
