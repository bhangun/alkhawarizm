package tech.kayys.alkhawarizm.core.nn;

import tech.kayys.alkhawarizm.core.tensor.Tensor;

public class ConvTranspose2d extends NNModule {
    public ConvTranspose2d(long inChannels, long outChannels, long kernelSize, int stride, int padding) {}
    public ConvTranspose2d(long inChannels, long outChannels, long kernelSize) {}
    
    @Override
    public Tensor forward(Tensor input) {
        return input; // placeholder
    }
}
