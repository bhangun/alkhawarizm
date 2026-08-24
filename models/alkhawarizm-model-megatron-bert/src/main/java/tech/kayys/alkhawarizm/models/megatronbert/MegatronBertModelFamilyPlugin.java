package tech.kayys.alkhawarizm.models.megatronbert;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MegatronBertModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "megatron_bert",
                "Megatron-BERT",
                List.of("megatron-bert", "megatron_bert"),
                List.of("MegatronBertModel", "MegatronBertForPreTraining",
                        "MegatronBertForMaskedLM", "MegatronBertForCausalLM",
                        "MegatronBertForSequenceClassification",
                        "MegatronBertForTokenClassification",
                        "MegatronBertForQuestionAnswering"),
                List.of( ModelFamilyCapability.CAUSAL_LM,
                        ModelFamilyCapability.TOKENIZER,
                        ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/megatron_bert",
                        "tokenizer", "bert_wordpiece",
                        "direct_safetensor", "pending_megatron_bert_encoder_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.wordPiece("megatron-bert-wordpiece"));
    }
}
