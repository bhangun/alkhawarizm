package tech.kayys.alkhawarizm.models.markuplm;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class MarkupLmModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "markuplm",
                "MarkupLM",
                List.of("markuplm"),
                List.of("MarkupLMModel", "MarkupLMForQuestionAnswering",
                        "MarkupLMForTokenClassification", "MarkupLMForSequenceClassification"),
                List.of( ModelFamilyCapability.TOKENIZER, ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/markuplm",
                        "tokenizer", "byte_level_bpe",
                        "processor", "html_xpath_processor",
                        "direct_safetensor", "pending_xpath_feature_processor_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.huggingFaceBpe("markuplm-byte-level-bpe"));
    }
}
