package tech.kayys.alkhawarizm.models.xlm;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class XlmModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "xlm",
                "XLM",
                List.of("xlm"),
                List.of("XLMModel", "XLMWithLMHeadModel",
                        "XLMForSequenceClassification", "XLMForTokenClassification",
                        "XLMForQuestionAnswering", "XLMForQuestionAnsweringSimple"),
                List.of(ModelFamilyCapability.CAUSAL_LM, ModelFamilyCapability.TOKENIZER, ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/xlm",
                        "tokenizer", "xlm_vocab_merges_bpe",
                        "direct_safetensor", "pending_xlm_language_embedding_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.huggingFaceBpe("xlm-bpe"));
    }
}
