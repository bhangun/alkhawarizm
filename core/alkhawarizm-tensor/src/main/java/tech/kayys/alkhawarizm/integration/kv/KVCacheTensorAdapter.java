package tech.kayys.alkhawarizm.integration.kv;

import tech.kayys.alkhawarizm.core.backend.ComputeBackend;
import tech.kayys.alkhawarizm.core.tensor.DefaultTensor;
import tech.kayys.alkhawarizm.core.tensor.DType;
import tech.kayys.alkhawarizm.core.tensor.Shape;
import tech.kayys.alkhawarizm.core.tensor.Tensor;
import tech.kayys.alkhawarizm.core.tensor.DeviceType;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;

/**
 * Adapter to produce an alkhawarizm Tensor backed by a PagedKVCache page
 * without copying.
 * Reflection is used to avoid compile-time dependency on gollek-core.
 */
public final class KVCacheTensorAdapter {

    private KVCacheTensorAdapter() {
    }

    public static Tensor tensorFromCache(Object cache, long key, Shape shape, DType dtype, ComputeBackend backend) {
        try {
            Method getView = cache.getClass().getMethod("getView", long.class);
            Method pin = cache.getClass().getMethod("pin", long.class);
            Method unpin = cache.getClass().getMethod("unpin", long.class);

            ByteBuffer pinned = (ByteBuffer) pin.invoke(cache, key);
            if (pinned == null)
                return null;

            ByteBufferBackedBuffer buf = new ByteBufferBackedBuffer(pinned, () -> {
                try {
                    unpin.invoke(cache, key);
                } catch (Throwable ignored) {
                }
            });

            DefaultTensor t = new DefaultTensor(shape, dtype, DeviceType.CPU, buf, backend);
            return t;
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("cache does not implement expected API (getView/pin/unpin)", e);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
