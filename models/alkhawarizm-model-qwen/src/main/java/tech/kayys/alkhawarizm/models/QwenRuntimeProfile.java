package tech.kayys.alkhawarizm.models;

import tech.kayys.alkhawarizm.spi.model.ModelAttentionTraitsPolicy;
import tech.kayys.alkhawarizm.spi.model.ModelConfig;
import tech.kayys.alkhawarizm.spi.model.ModelPromptTraits;
import tech.kayys.alkhawarizm.spi.model.ModelRuntimeTraits;

/**
 * Qwen-specific runtime policy for prompt and attention behavior.
 *
 * <p>
 * The architecture adapters own this family profile so new Qwen variants can
 * evolve here instead of scattering runtime defaults across each adapter.
 * </p>
 */
public final class QwenRuntimeProfile {

    public static final String QWEN_DEFAULT_SYSTEM_PROMPT = "You are Qwen, created by Alibaba Cloud. You are a helpful assistant.";

    public static ModelRuntimeTraits text(ModelConfig config) {
        boolean compact = ModelAttentionTraitsPolicy.isCompactAttentionMatvecCandidate(config);
        return ModelRuntimeTraits.builder()
                .prompt(prompt())
                .attention(new ModelRuntimeTraits.AttentionRuntimeTraits(false, false, false, false, false, false, false, compact ? 128 : 0, compact, ModelAttentionTraitsPolicy.isLargeAttentionMatvecCandidate(config, false, false), false))
                .build();
    }

    public static ModelPromptTraits prompt() {
        return new ModelPromptTraits(
                ModelRuntimeTraits.PromptBosPolicy.DEFAULT,
                java.util.Collections.<String>emptySet(),
                false,
                false,
                false,
                QWEN_DEFAULT_SYSTEM_PROMPT,
                java.util.Collections.<String>emptySet(),
                false);
    }
}
