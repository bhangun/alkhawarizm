package tech.kayys.alkhawarizm.core.memory;

import org.junit.jupiter.api.Test;

import java.lang.foreign.Arena;
import java.lang.foreign.MemorySegment;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Phase 3: Session-scoped Arena Memory Pool.
 */
class MemoryPoolPhase3Test {

    // ── OffHeapBufferPool ──────────────────────────────────────────────────────

    @Test
    void poolAcquireAndReleaseReusesSameMemory() {
        try (OffHeapBufferPool pool = new OffHeapBufferPool()) {
            MemorySegment seg1 = pool.acquire(256);
            assertNotNull(seg1);
            assertTrue(seg1.byteSize() >= 256);

            pool.release(seg1);
            assertEquals(1, pool.misses()); // first acquire is always a miss

            MemorySegment seg2 = pool.acquire(256);
            assertEquals(1, pool.hits()); // second acquire must be a hit
            assertEquals(seg1.address(), seg2.address(), "same native address should be reused");
        }
    }

    @Test
    void poolBucketAlignsSizeToPowerOfTwo() {
        try (OffHeapBufferPool pool = new OffHeapBufferPool()) {
            // Request 100 bytes — should be rounded up to the 128-byte bucket.
            MemorySegment seg = pool.acquire(100);
            assertTrue(seg.byteSize() >= 128, "size should be rounded up to next power of 2");
        }
    }

    @Test
    void poolLargeAllocationBypassesBuckets() {
        // 128 MiB exceeds MAX_BUCKET_BYTES — must not be pooled.
        try (OffHeapBufferPool pool = new OffHeapBufferPool()) {
            long bigSize = 128L * 1024 * 1024;
            MemorySegment seg = pool.acquire(bigSize);
            assertTrue(seg.byteSize() >= bigSize);
            pool.release(seg); // should be silently ignored (no bucket for this size)
            // A second acquire should still be a miss (not recycled).
            pool.acquire(bigSize);
            assertEquals(2, pool.misses());
            assertEquals(0, pool.hits());
        }
    }

    @Test
    void poolStatsReflectHitsAndMisses() {
        try (OffHeapBufferPool pool = new OffHeapBufferPool()) {
            MemorySegment seg = pool.acquire(512);
            pool.release(seg);
            pool.acquire(512);

            String stats = pool.stats();
            assertTrue(stats.contains("hits=1"), stats);
            assertTrue(stats.contains("misses=1"), stats);
        }
    }

    // ── ManagedArena ──────────────────────────────────────────────────────────

    @Test
    void managedArenaClosesOnlyWhenRefCountReachesZero() {
        ManagedArena ma = ManagedArena.ofShared();
        assertEquals(1, ma.refCount());

        ma.retain();
        assertEquals(2, ma.refCount());
        assertTrue(ma.isOpen());

        ma.close(); // drops consumer reference
        assertEquals(1, ma.refCount());
        assertTrue(ma.isOpen(), "should still be open — producer holds a reference");

        ma.close(); // drops producer reference → frees memory
        assertEquals(0, ma.refCount());
        assertFalse(ma.isOpen());
    }

    @Test
    void managedArenaAllocatesMemory() {
        try (ManagedArena ma = ManagedArena.ofShared()) {
            MemorySegment seg = ma.allocate(1024);
            assertEquals(1024, seg.byteSize());
        }
    }

    @Test
    void managedArenaRetainOnClosedThrows() {
        ManagedArena ma = ManagedArena.ofShared();
        ma.close();
        assertThrows(IllegalStateException.class, ma::retain);
    }

    // ── InferenceSession ──────────────────────────────────────────────────────

    @Test
    void inferenceSessionClosesPoolOnExit() {
        OffHeapBufferPool[] poolRef = { null };
        try (InferenceSession session = InferenceSession.of(pool -> {
            poolRef[0] = pool;
            return new DummyBackend(pool);
        })) {
            assertNotNull(session.backend());
            assertNotNull(session.pool());
        }
        // Pool arena is closed — further allocations would fail.
        // We only check that close() didn't throw.
    }

    @Test
    void inferenceSessionRejectsCallsAfterClose() {
        InferenceSession session = InferenceSession.of(
                pool -> new DummyBackend(pool));
        session.close();
        assertThrows(IllegalStateException.class, session::backend);
    }

    // A simple stub to avoid circular dependency on CpuBackend
    static class DummyBackend implements tech.kayys.alkhawarizm.core.backend.ComputeBackend {
        public DummyBackend(OffHeapBufferPool pool) {
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor add(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                tech.kayys.alkhawarizm.core.tensor.Tensor b) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor sub(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                tech.kayys.alkhawarizm.core.tensor.Tensor b) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor mul(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                float scalar) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor mul(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                tech.kayys.alkhawarizm.core.tensor.Tensor b) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor div(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                float scalar) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor div(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                tech.kayys.alkhawarizm.core.tensor.Tensor b) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor addScalar(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                float scalar) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor matmul(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                tech.kayys.alkhawarizm.core.tensor.Tensor b) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor reshape(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                long... shape) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor attention(tech.kayys.alkhawarizm.core.tensor.Tensor Q,
                tech.kayys.alkhawarizm.core.tensor.Tensor K, tech.kayys.alkhawarizm.core.tensor.Tensor V) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor softmax(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor slice(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                long[] offsets, long[] sizes) {
            return null;
        }

        @Override
        public java.util.List<tech.kayys.alkhawarizm.core.tensor.Tensor> split(
                tech.kayys.alkhawarizm.core.tensor.Tensor a, int axis, int parts) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor pow(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                float exponent) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor mean(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor abs(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor crossEntropy(tech.kayys.alkhawarizm.core.tensor.Tensor pred,
                tech.kayys.alkhawarizm.core.tensor.Tensor target) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor binaryCrossEntropy(
                tech.kayys.alkhawarizm.core.tensor.Tensor pred, tech.kayys.alkhawarizm.core.tensor.Tensor target) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor cast(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                tech.kayys.alkhawarizm.core.tensor.DType dtype) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor to(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                tech.kayys.alkhawarizm.core.tensor.DeviceType device) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor zerosLike(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor sqrt(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor relu(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor sigmoid(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor tanh(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor log(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor exp(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor silu(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor flatten(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor unsqueeze(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                int dim) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor squeeze(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor transpose(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor transpose(tech.kayys.alkhawarizm.core.tensor.Tensor a, int d0,
                int d1) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor gelu(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor softmax(tech.kayys.alkhawarizm.core.tensor.Tensor a, int dim) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor logSoftmax(tech.kayys.alkhawarizm.core.tensor.Tensor a,
                int dim) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor mean(tech.kayys.alkhawarizm.core.tensor.Tensor a, int dim,
                boolean keepDim) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor sum(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor sum(tech.kayys.alkhawarizm.core.tensor.Tensor a, int dim,
                boolean keepDim) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor max(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor layerNorm(tech.kayys.alkhawarizm.core.tensor.Tensor input,
                long[] normalizedShape, tech.kayys.alkhawarizm.core.tensor.Tensor weight,
                tech.kayys.alkhawarizm.core.tensor.Tensor bias, float eps) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor rmsNorm(tech.kayys.alkhawarizm.core.tensor.Tensor input,
                tech.kayys.alkhawarizm.core.tensor.Tensor weight, float eps) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor batchNorm(tech.kayys.alkhawarizm.core.tensor.Tensor input,
                tech.kayys.alkhawarizm.core.tensor.Tensor weight, tech.kayys.alkhawarizm.core.tensor.Tensor bias,
                tech.kayys.alkhawarizm.core.tensor.Tensor runningMean,
                tech.kayys.alkhawarizm.core.tensor.Tensor runningVar, boolean training, float momentum, float eps) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor conv2d(tech.kayys.alkhawarizm.core.tensor.Tensor input,
                tech.kayys.alkhawarizm.core.tensor.Tensor weight, tech.kayys.alkhawarizm.core.tensor.Tensor bias,
                int stride, int padding, int dilation, int groups) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor maxPool2d(tech.kayys.alkhawarizm.core.tensor.Tensor input,
                int kernelSize, int stride, int padding) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor adaptiveAvgPool2d(
                tech.kayys.alkhawarizm.core.tensor.Tensor input, int outputH, int outputW) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor dropout(tech.kayys.alkhawarizm.core.tensor.Tensor input,
                float p, boolean training) {
            return null;
        }

        @Override
        public tech.kayys.alkhawarizm.core.tensor.Tensor embedding(tech.kayys.alkhawarizm.core.tensor.Tensor weight,
                tech.kayys.alkhawarizm.core.tensor.Tensor input, long paddingIdx) {
            return null;
        }

        @Override
        public long numel(tech.kayys.alkhawarizm.core.tensor.Tensor a) {
            return 0;
        }
    }
}
