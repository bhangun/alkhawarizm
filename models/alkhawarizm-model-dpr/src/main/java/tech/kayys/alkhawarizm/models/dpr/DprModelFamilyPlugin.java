package tech.kayys.alkhawarizm.models.dpr;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DprModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "dpr",
                "Dense Passage Retrieval",
                List.of("dpr"),
                List.of("DPRContextEncoder", "DPRQuestionEncoder", "DPRReader",
                        "DPRPretrainedContextEncoder", "DPRPretrainedQuestionEncoder",
                        "DPRPretrainedReader"),
                List.of(ModelFamilyCapability.TOKENIZER,
                        ModelFamilyCapability.EMBEDDING),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/dpr",
                        "tokenizer", "wordpiece",
                        "direct_safetensor", "pending_dpr_dual_encoder_retrieval_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.wordPiece("dpr-wordpiece"));
    }
}
