package tech.kayys.alkhawarizm.models.codegen;

import jakarta.enterprise.context.ApplicationScoped;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyCapability;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyDescriptor;
import tech.kayys.alkhawarizm.spi.model.ModelFamilyPlugin;
import tech.kayys.alkhawarizm.spi.model.ModelTokenizerDescriptor;

import java.util.List;
import java.util.Map;

@ApplicationScoped
public class CodeGenModelFamilyPlugin implements ModelFamilyPlugin {

    @Override
    public ModelFamilyDescriptor descriptor() {
        return new ModelFamilyDescriptor(
                "codegen",
                "Salesforce CodeGen",
                List.of("codegen"),
                List.of("CodeGenForCausalLM", "CodeGenModel"),
                List.of(ModelFamilyCapability.CAUSAL_LM, ModelFamilyCapability.TOKENIZER),
                Map.of(
                        "bundle_profile", "metadata_only",
                        "tokenizer", "byte_level_bpe",
                        "direct_safetensor", "pending_codegen_rotary_gptj_style_attention_runtime",
                        "version", "0.1.0-SNAPSHOT"));
    }

    @Override
    public List<ModelTokenizerDescriptor> tokenizerDescriptors() {
        return List.of(ModelTokenizerDescriptor.huggingFaceBpe("codegen-byte-level-bpe"));
    }
}
