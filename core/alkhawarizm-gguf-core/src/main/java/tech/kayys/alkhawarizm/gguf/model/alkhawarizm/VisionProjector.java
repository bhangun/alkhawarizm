package tech.kayys.alkhawarizm.gguf.model.alkhawarizm;

import tech.kayys.alkhawarizm.core.nn.Linear;
import tech.kayys.alkhawarizm.core.nn.NNModule;
import tech.kayys.alkhawarizm.core.tensor.Tensor;
import tech.kayys.alkhawarizm.core.tensor.WeightAdapter;
/**
 * 
 * Core class for alkhawarizm module.
 *
 * <p>Key functionality:
 * <ul>
 * <li>Provides core class operations
 * </ul>
 *
 * @author bhangun
 * @since 0.1.0
 */
public class VisionProjector extends NNModule {
    private final Linear proj;

    public VisionProjector(WeightAdapter weights) {
        // PaliGemma uses a single Linear projection (often bias=true)
        Tensor wProj = weights.getWeight("mm_proj.weight");
        Tensor bProj = weights.getWeight("mm_proj.bias");

        proj = new Linear(wProj, bProj);
        registerModule("proj", proj);
    }

    @Override
    public Tensor forward(Tensor x) {
        // x: [batch, num_patches, embed_dim]
        // PaliGemma usually applies the projector directly to the normalized patch
        // embeddings
        return proj.forward(x);
    }
}
