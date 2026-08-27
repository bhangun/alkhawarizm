package tech.kayys.alkhawarizm.autograd;

import tech.kayys.gollek.ir.*;
import java.util.Map;
/**
 * Core interface for tech module.
 *
 * @author bhangun
 * @since 0.1.0
 */
public interface GradFn {
    /**
     *
     * Compute gradients for inputs of an op
     * 
     * @param op      forward op
     * @param gradOut gradient of output
     * @param ctx     grad context (for emitting ops)
     * @return map: input → gradient
     */
    Map<GValueId, GValueId> backward(
            GOp op,
            GValueId gradOut,
            GradContext ctx);
}