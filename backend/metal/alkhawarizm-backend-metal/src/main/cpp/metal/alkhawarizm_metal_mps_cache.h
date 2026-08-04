/**
 * alkhawarizm_metal_mps_cache.h — cached MPS descriptors and kernels.
 */

#ifndef ALKHAWARIZM_METAL_MPS_CACHE_H
#define ALKHAWARIZM_METAL_MPS_CACHE_H

#import <Foundation/Foundation.h>
#import <MetalPerformanceShaders/MetalPerformanceShaders.h>

void alkhawarizm_metal_mps_cache_init(BOOL disabled);

MPSMatrixDescriptor *alkhawarizm_metal_cached_matrix_descriptor(
    int rows, int cols, NSUInteger row_bytes, MPSDataType data_type);

MPSVectorDescriptor *
alkhawarizm_metal_cached_vector_descriptor(int length, MPSDataType data_type);

MPSMatrixMultiplication *alkhawarizm_metal_cached_mmul(
    BOOL transpose_left, BOOL transpose_right, int result_rows, int result_cols,
    int interior_cols, MPSDataType left_type, MPSDataType right_type,
    MPSDataType result_type, float alpha, float beta);

MPSMatrixVectorMultiplication *
alkhawarizm_metal_cached_mvec(BOOL transpose, int rows, int columns,
                              double alpha, double beta);

#define cached_matrix_descriptor alkhawarizm_metal_cached_matrix_descriptor
#define cached_vector_descriptor alkhawarizm_metal_cached_vector_descriptor
#define cached_mmul alkhawarizm_metal_cached_mmul
#define cached_mvec alkhawarizm_metal_cached_mvec

#endif
