
package tech.kayys.alkhawarizm.autograd.spi;

import tech.kayys.alkhawarizm.autograd.GradFn;
import tech.kayys.alkhawarizm.autograd.GradRegistry;

public interface GradFnProvider {
    void registerGradients(GradRegistry registry);
}
