package tech.kayys.alkhawarizm.models.persimmon;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PersimmonModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "persimmon",
                "Adept Persimmon",
                List.of("persimmon"),
                List.of("PersimmonForCausalLM", "PersimmonModel",
                        "PersimmonForSequenceClassification", "PersimmonForTokenClassification"),
                List.of(ModelFamilyCapability.CAUSAL_LM, ModelFamilyCapability.TOKENIZER),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/persimmon",
                        "tokenizer", "tokenizer_json_bpe",
                        "direct_safetensor", "pending_persimmon_rotary_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.huggingFaceBpe("persimmon-hf-bpe"));
    }
}
