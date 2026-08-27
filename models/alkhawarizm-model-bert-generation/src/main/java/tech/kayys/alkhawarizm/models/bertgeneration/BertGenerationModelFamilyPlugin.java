package tech.kayys.alkhawarizm.models.bertgeneration;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class BertGenerationModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "bert_generation",
                "BERT Generation",
                List.of("bert_generation", "bert-generation"),
                List.of("BertGenerationEncoder", "BertGenerationDecoder", "BertGenerationTokenizer"),
                List.of(
                        ModelFamilyCapability.TOKENIZER),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "tokenizer", "sentencepiece",
                        "direct_safetensor", "pending_encoder_decoder_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.sentencePieceBpe("bert-generation-spm-bpe"));
    }
}
