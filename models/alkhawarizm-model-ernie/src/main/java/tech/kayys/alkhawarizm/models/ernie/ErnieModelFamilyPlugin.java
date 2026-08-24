package tech.kayys.alkhawarizm.models.ernie;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ErnieModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "ernie",
                "ERNIE",
                List.of("ernie"),
                List.of("ErnieModel", "ErnieForPreTraining", "ErnieForCausalLM",
                        "ErnieForMaskedLM", "ErnieForSequenceClassification",
                        "ErnieForTokenClassification", "ErnieForQuestionAnswering"),
                List.of( ModelFamilyCapability.TOKENIZER, ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/ernie",
                        "tokenizer", "wordpiece",
                        "direct_safetensor", "pending_task_id_bert_variant_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.wordPiece("ernie-wordpiece"));
    }
}
