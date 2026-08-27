package tech.kayys.alkhawarizm.spi.model;

import tech.kayys.alkhawarizm.spi.spec.*;
import tech.kayys.alkhawarizm.core.tensor.DeviceType;
import tech.kayys.alkhawarizm.core.model.ModelFormat;

/**
 * Resource utilization metrics for a model runner
 * @author bhangun
 */
public record ResourceMetrics(
                long cpuUsagePercent,
                long memoryUsageBytes,
                long gpuUsagePercent,
                long vramUsageBytes,
                int activeRequests) {
}
