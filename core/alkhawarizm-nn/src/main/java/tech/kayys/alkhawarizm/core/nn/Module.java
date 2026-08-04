package tech.kayys.alkhawarizm.core.nn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.lang.foreign.MemorySegment;
import tech.kayys.alkhawarizm.core.tensor.Tensor;
import tech.kayys.alkhawarizm.core.tensor.DeviceType;
import tech.kayys.alkhawarizm.core.tensor.DefaultTensor;

/**
 * Abstract base class for all neural network modules in Aljabr.
 * Manages parameters, buffers, and submodules.
 */
public abstract class Module implements AutoCloseable {

    protected final Map<String, Tensor> parameters = new LinkedHashMap<>();
    protected final Map<String, Tensor> buffers = new LinkedHashMap<>();
    protected final Map<String, Module> submodules = new LinkedHashMap<>();
    protected boolean training = true;

    protected void registerParameter(String name, Tensor param) {
        Objects.requireNonNull(name, "Parameter name must not be null");
        parameters.put(name, param);
    }

    protected void registerBuffer(String name, Tensor buffer) {
        Objects.requireNonNull(name, "Buffer name must not be null");
        buffers.put(name, buffer);
    }

    protected void registerModule(String name, Module module) {
        Objects.requireNonNull(name, "Module name must not be null");
        Objects.requireNonNull(module, "Module must not be null");
        submodules.put(name, module);
    }

    public abstract Tensor forward(Tensor input);

    public Module train(boolean mode) {
        this.training = mode;
        submodules.values().forEach(m -> m.train(mode));
        return this;
    }

    public Module train() {
        return train(true);
    }

    public Module eval() {
        return train(false);
    }

    public boolean isTraining() {
        return training;
    }

    public Module to(DeviceType device) {
        parameters.replaceAll((name, param) -> param.to(device));
        buffers.replaceAll((name, buf) -> buf.to(device));
        submodules.values().forEach(m -> m.to(device));
        return this;
    }

    public void zeroGrad() {
        parameters.values().forEach(p -> {
            if (p.grad() != null) {
                p.setGrad(p.grad().zerosLike());
            }
        });
        submodules.values().forEach(Module::zeroGrad);
    }

    public List<Tensor> getParameters() {
        List<Tensor> all = new ArrayList<>(parameters.values());
        submodules.values().forEach(m -> all.addAll(m.getParameters()));
        return Collections.unmodifiableList(all);
    }

    public Map<String, Tensor> namedParameters() {
        Map<String, Tensor> result = new LinkedHashMap<>(parameters);
        submodules.forEach((name, module) -> module.namedParameters()
                .forEach((pName, param) -> result.put(name + "." + pName, param)));
        return Collections.unmodifiableMap(result);
    }

    public Map<String, Module> children() {
        return Collections.unmodifiableMap(submodules);
    }

    public long parameterCount() {
        long count = 0;
        for (Tensor p : parameters.values()) {
            count += p.shape().numel();
        }
        for (Module m : submodules.values()) {
            count += m.parameterCount();
        }
        return count;
    }

    public void loadStateDict(Map<String, Tensor> stateDict) {
        Map<String, Tensor> current = namedParameters();
        for (Map.Entry<String, Tensor> entry : stateDict.entrySet()) {
            Tensor src = entry.getValue();
            Tensor dst = current.get(entry.getKey());
            if (dst != null) {
                if (src.shape().numel() != dst.shape().numel()) {
                    throw new IllegalArgumentException("Shape mismatch for parameter " + entry.getKey() +
                            ": expected " + dst.shape() + " but got " + src.shape());
                }
                if (src instanceof DefaultTensor && dst instanceof DefaultTensor) {
                    MemorySegment srcSeg = ((DefaultTensor) src).buffer().segment();
                    MemorySegment dstSeg = ((DefaultTensor) dst).buffer().segment();
                    MemorySegment.copy(srcSeg, 0, dstSeg, 0, srcSeg.byteSize());
                } else {
                    throw new UnsupportedOperationException("In-place loadStateDict only supported for DefaultTensor");
                }
            }
        }
    }

    @Override
    public void close() {
        parameters.values().forEach(Tensor::release);
        buffers.values().forEach(Tensor::release);
        submodules.values().forEach(Module::close);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(training=" + training + ")";
    }
}
