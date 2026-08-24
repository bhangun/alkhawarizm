package tech.kayys.alkhawarizm.models.distilbert;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DistilBertModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "distilbert",
                "DistilBERT",
                List.of("distilbert"),
                List.of("DistilBertModel", "DistilBertForMaskedLM",
                        "DistilBertForSequenceClassification", "DistilBertForQuestionAnswering",
                        "DistilBertForTokenClassification", "DistilBertForMultipleChoice"),
                List.of( ModelFamilyCapability.TOKENIZER, ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/distilbert",
                        "tokenizer", "wordpiece",
                        "direct_safetensor", "not_causal_lm_distilled_encoder_runtime_pending",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.wordPiece("distilbert-wordpiece"));
    }
}
