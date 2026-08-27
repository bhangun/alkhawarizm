package tech.kayys.alkhawarizm.models.olmo;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class OlmoModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "olmo",
                "AllenAI OLMo",
                List.of("olmo"),
                List.of("OlmoForCausalLM", "OlmoModel"),
                List.of(ModelFamilyCapability.CAUSAL_LM, ModelFamilyCapability.TOKENIZER,
                        ModelFamilyCapability.CHAT_TEMPLATE),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "tokenizer", "byte_level_bpe",
                        "direct_safetensor", "pending_olmo_norm_and_attention_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.huggingFaceBpe("olmo-byte-level-bpe"));
    }
}
