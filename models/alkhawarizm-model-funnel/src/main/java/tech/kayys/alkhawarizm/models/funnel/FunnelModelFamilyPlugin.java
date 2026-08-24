package tech.kayys.alkhawarizm.models.funnel;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class FunnelModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "funnel",
                "Funnel Transformer",
                List.of("funnel"),
                List.of("FunnelBaseModel", "FunnelModel", "FunnelForPreTraining",
                        "FunnelForMaskedLM", "FunnelForSequenceClassification",
                        "FunnelForTokenClassification", "FunnelForQuestionAnswering"),
                List.of( ModelFamilyCapability.TOKENIZER, ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/funnel",
                        "tokenizer", "wordpiece",
                        "direct_safetensor", "pending_pooling_encoder_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.wordPiece("funnel-wordpiece"));
    }
}
