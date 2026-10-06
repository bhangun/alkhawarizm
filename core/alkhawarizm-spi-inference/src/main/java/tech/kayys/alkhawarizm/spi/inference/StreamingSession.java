package tech.kayys.alkhawarizm.spi.inference;

import io.smallrye.mutiny.Multi;
import tech.kayys.alkhawarizm.spi.Message;
import tech.kayys.alkhawarizm.spi.auth.ApiKeyConstants;
import tech.kayys.alkhawarizm.spi.context.RequestContext;

public record StreamingSession(
        String sessionId,
        String modelId,
        RequestContext requestContext,
        Multi<Message> stream) {
    public String apiKey() {
        if (requestContext.apiKey() == null || requestContext.apiKey().isBlank()) {
            return ApiKeyConstants.COMMUNITY_API_KEY;
        }
        return requestContext.apiKey();
    }
}
