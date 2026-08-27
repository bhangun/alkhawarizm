
package tech.kayys.alkhawarizm.autograd.spi;

import tech.kayys.alkhawarizm.autograd.GradFn;
import tech.kayys.alkhawarizm.autograd.GradRegistry;
/**
 * 
 * Core interface for kayys module.
 *
 * @author bhangun
 * @since 0.1.0
 */
public interface GradFnProvider {
    void registerGradients(GradRegistry registry);
}
