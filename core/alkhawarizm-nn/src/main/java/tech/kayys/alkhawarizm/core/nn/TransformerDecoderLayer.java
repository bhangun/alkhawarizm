package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class TransformerDecoderLayer extends NNModule {
    private final int dModel;
    private final int nHead;
    private final int dimFeedforward;
    private final float dropout;

    public TransformerDecoderLayer(int dModel, int nHead, int dimFeedforward, float dropout) {
        this.dModel = dModel;
        this.nHead = nHead;
        this.dimFeedforward = dimFeedforward;
        this.dropout = dropout;
        
        registerModule("self_attn", new MultiheadAttention(dModel, nHead));
        registerModule("multihead_attn", new MultiheadAttention(dModel, nHead));
        registerModule("linear1", new Linear(dModel, dimFeedforward));
        registerModule("linear2", new Linear(dimFeedforward, dModel));
        registerModule("norm1", new LayerNorm(dModel));
        registerModule("norm2", new LayerNorm(dModel));
        registerModule("norm3", new LayerNorm(dModel));
    }

    @Override
    public Tensor forward(Tensor input) {
        // Required for basic NNModule
        return forward(input, input);
    }
    
    public Tensor forward(Tensor tgt, Tensor memory) {
        // Placeholder
        return tgt;
    }
}
