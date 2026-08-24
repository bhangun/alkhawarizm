package tech.kayys.alkhawarizm.models.shieldgemma2;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class ShieldGemma2ModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "shieldgemma2",
                "Google ShieldGemma 2",
                List.of("shieldgemma2"),
                List.of("ShieldGemma2ForImageClassification", "ShieldGemma2Processor"),
                List.of(ModelFamilyCapability.VISION, ModelFamilyCapability.MULTIMODAL,
                        ModelFamilyCapability.TOKENIZER),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/shieldgemma2",
                        "tokenizer", "gemma3_sentencepiece_with_shieldgemma_processor",
                        "direct_safetensor", "pending_image_safety_classifier_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.sentencePieceBpe("shieldgemma2-spm-bpe"));
    }
}
