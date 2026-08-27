package tech.kayys.alkhawarizm.models.squeezebert;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class SqueezeBertModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "squeezebert",
                "SqueezeBERT",
                List.of("squeezebert"),
                List.of("SqueezeBertModel", "SqueezeBertForMaskedLM",
                        "SqueezeBertForSequenceClassification",
                        "SqueezeBertForTokenClassification",
                        "SqueezeBertForQuestionAnswering"),
                List.of( ModelFamilyCapability.TOKENIZER, ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "tokenizer", "bert_wordpiece_alias",
                        "direct_safetensor", "not_causal_lm_squeezebert_runtime_pending",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.wordPiece("squeezebert-wordpiece"));
    }
}
