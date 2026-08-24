package tech.kayys.alkhawarizm.models.olmoe;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OlmoeModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "olmoe",
                "AllenAI OLMoE",
                List.of("olmoe", "olmo_moe", "olmo-moe"),
                List.of("OlmoeForCausalLM", "OlmoeModel"),
                List.of(ModelFamilyCapability.CAUSAL_LM, ModelFamilyCapability.TOKENIZER,
                        ModelFamilyCapability.CHAT_TEMPLATE),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/olmoe",
                        "tokenizer", "byte_level_bpe",
                        "direct_safetensor", "pending_olmoe_sparse_moe_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.huggingFaceBpe("olmoe-byte-level-bpe"));
    }
}
