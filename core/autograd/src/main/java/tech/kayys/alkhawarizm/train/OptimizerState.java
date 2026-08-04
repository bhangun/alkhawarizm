package tech.kayys.alkhawarizm.train;

import tech.kayys.alkhawarizm.core.tensor.Tensor;
import java.util.HashMap;
import java.util.Map;

public final class OptimizerState {
    public final Map<String, Tensor> m = new HashMap<>(); // first moment
    public final Map<String, Tensor> v = new HashMap<>(); // second moment
    public int step = 0;
}