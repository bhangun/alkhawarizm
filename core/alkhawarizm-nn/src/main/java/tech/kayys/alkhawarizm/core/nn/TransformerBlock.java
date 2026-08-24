package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class TransformerBlock extends NNModule {
    public TransformerBlock(int dModel, int nHeads, int dFF, float dropout) {}
    
    @Override
    public Tensor forward(Tensor input) {
        return input;
    }
}
