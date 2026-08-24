package tech.kayys.alkhawarizm.models.t5gemma;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class T5GemmaModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "t5gemma",
                "Google T5Gemma",
                List.of("t5gemma", "t5_gemma_module"),
                List.of("T5GemmaForConditionalGeneration", "T5GemmaModel", "T5GemmaEncoderModel",
                        "T5GemmaForSequenceClassification", "T5GemmaForTokenClassification"),
                List.of(
                        ModelFamilyCapability.TOKENIZER),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "origin", "3rdparty/transformers/src/transformers/models/t5gemma",
                        "tokenizer", "gemma_sentencepiece_seq2seq",
                        "direct_safetensor", "pending_seq2seq_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.sentencePieceBpe("t5gemma-spm-bpe"));
    }
}
