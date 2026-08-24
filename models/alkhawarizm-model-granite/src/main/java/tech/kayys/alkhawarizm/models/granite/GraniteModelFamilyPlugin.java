package tech.kayys.alkhawarizm.models.granite;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class GraniteModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "granite",
                "IBM Granite",
                List.of("granite", "granitemoe", "granitemoehybrid", "granitemoeshared"),
                List.of("GraniteForCausalLM", "GraniteModel", "GraniteMoeForCausalLM",
                        "GraniteMoeModel", "GraniteMoeHybridForCausalLM", "GraniteMoeHybridModel",
                        "GraniteMoeSharedForCausalLM", "GraniteMoeSharedModel"),
                List.of(ModelFamilyCapability.CAUSAL_LM, ModelFamilyCapability.TOKENIZER,
                        ModelFamilyCapability.CHAT_TEMPLATE),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/granite,granitemoe,granitemoehybrid,granitemoeshared",
                        "tokenizer", "hf_tokenizer_json_or_bpe",
                        "direct_safetensor", "pending_granite_moe_and_hybrid_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.huggingFaceBpe("granite-tokenizer-json-or-bpe"));
    }
}
