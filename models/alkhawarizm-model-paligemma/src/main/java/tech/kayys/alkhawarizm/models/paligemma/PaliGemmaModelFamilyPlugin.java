package tech.kayys.alkhawarizm.models.paligemma;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class PaliGemmaModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "paligemma",
                "Google PaliGemma",
                List.of("paligemma"),
                List.of("PaliGemmaForConditionalGeneration", "PaliGemmaModel"),
                List.of(ModelFamilyCapability.CAUSAL_LM, ModelFamilyCapability.TOKENIZER,
                        ModelFamilyCapability.CHAT_TEMPLATE, ModelFamilyCapability.VISION,
                        ModelFamilyCapability.MULTIMODAL),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/paligemma",
                        "tokenizer", "gemma_sentencepiece_with_siglip_processor",
                        "direct_safetensor", "pending_paligemma_vision_projector_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.sentencePieceBpe("paligemma-spm-bpe"));
    }
}
