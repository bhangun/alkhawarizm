package tech.kayys.alkhawarizm.models.falconh1;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FalconH1ModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "falcon_h1",
                "TII Falcon-H1",
                List.of("falcon_h1", "falcon-h1"),
                List.of("FalconH1ForCausalLM", "FalconH1Model"),
                List.of(ModelFamilyCapability.CAUSAL_LM, ModelFamilyCapability.TOKENIZER,
                        ModelFamilyCapability.CHAT_TEMPLATE),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/falcon_h1",
                        "tokenizer", "tokenizer_json_bpe",
                        "direct_safetensor", "pending_falcon_h1_hybrid_state_space_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.huggingFaceBpe("falcon-h1-hf-bpe"));
    }
}
