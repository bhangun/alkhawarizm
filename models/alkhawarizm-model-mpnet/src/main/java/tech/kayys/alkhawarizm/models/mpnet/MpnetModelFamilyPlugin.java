package tech.kayys.alkhawarizm.models.mpnet;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MpnetModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "mpnet",
                "MPNet",
                List.of("mpnet"),
                List.of("MPNetModel", "MPNetForMaskedLM", "MPNetForSequenceClassification",
                        "MPNetForQuestionAnswering", "MPNetForTokenClassification"),
                List.of( ModelFamilyCapability.TOKENIZER, ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/mpnet",
                        "tokenizer", "wordpiece",
                        "direct_safetensor", "not_causal_lm",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.wordPiece("mpnet-wordpiece"));
    }
}
