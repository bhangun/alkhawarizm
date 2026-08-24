package tech.kayys.alkhawarizm.models.gemma3;

import tech.kayys.alkhawarizm.spi.model.ModelAttentionTraitsPolicy;
import tech.kayys.alkhawarizm.spi.model.ModelConfig;
import tech.kayys.alkhawarizm.spi.model.ModelPromptTraits;
import tech.kayys.alkhawarizm.spi.model.ModelRuntimeTraits;

/**
 * Gemma 3 text runtime policy for prompt/BOS and split-RoPE attention behavior.
 */
public final class Gemma3RuntimeProfile {

    public static ModelRuntimeTraits text(ModelConfig config) {
        return ModelRuntimeTraits.builder()
                .geluGatedFfn()
                .prompt(prompt())
                .attention(new ModelRuntimeTraits.AttentionRuntimeTraits(true, false, false, false, false, false, false, 0, false, false, false))
                .build();
    }

    public static ModelPromptTraits prompt() {
        return new ModelPromptTraits(
                ModelRuntimeTraits.PromptBosPolicy.TURN_AWARE,
                java.util.Collections.<String>emptySet(),
                false,
                false,
                false,
                ModelRuntimeTraits.DEFAULT_SYSTEM_PROMPT,
                java.util.Collections.<String>emptySet(),
                false);
    }
}
