package tech.kayys.alkhawarizm.models.lxmert;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class LxmertModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "lxmert",
                "LXMERT",
                List.of("lxmert"),
                List.of("LxmertModel", "LxmertForPreTraining", "LxmertForQuestionAnswering"),
                List.of( ModelFamilyCapability.TOKENIZER, ModelFamilyCapability.VISION,
                        ModelFamilyCapability.MULTIMODAL),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "tokenizer", "wordpiece",
                        "direct_safetensor", "pending_lxmert_cross_attention_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.wordPiece("lxmert-wordpiece"));
    }
}
