/**
 * alkhawarizm_metal_matvec_tuning.h — matvec thread-width and fast-path policy
 * helpers.
 */

#ifndef ALKHAWARIZM_METAL_MATVEC_TUNING_H
#define ALKHAWARIZM_METAL_MATVEC_TUNING_H

#import <Foundation/Foundation.h>
#import <Metal/Metal.h>
#include <stdint.h>

static const NSUInteger ALKHAWARIZM_MATVEC_THREADS_128 = 128;
static const NSUInteger ALKHAWARIZM_MATVEC_THREADS_256 = 256;

BOOL alkhawarizm_metal_prefer_matvec_128(int K, int max_output);
BOOL alkhawarizm_metal_use_matvec_128(id<MTLComputePipelineState> pipeline128,
                                      int K, int max_output);
BOOL alkhawarizm_metal_should_use_bf16_matvec_x4(int K, int max_output);
BOOL alkhawarizm_metal_should_use_bf16_matvec_x8(int K, int max_output);
BOOL alkhawarizm_metal_should_use_simdgroup_reduction(void);
BOOL alkhawarizm_metal_should_use_bf16_pair_simd_reduction(int K,
                                                           int max_output);
BOOL alkhawarizm_metal_should_use_fused_gated_ffn_matvec(int is_bf16,
                                                         int input_dim,
                                                         int intermediate_dim);

NSString *alkhawarizm_metal_matvec_shape_key(const char *op, int K, int N0,
                                             int N1, int N2);
NSUInteger alkhawarizm_metal_cached_matvec_threads(NSString *key);
void alkhawarizm_metal_cache_matvec_threads(NSString *key, NSUInteger threads);
NSUInteger alkhawarizm_metal_forced_matvec_threads(void);
BOOL alkhawarizm_metal_matvec_autotune_enabled(int K, int max_output,
                                               BOOL can128);
NSUInteger alkhawarizm_metal_default_matvec_threads(
    id<MTLComputePipelineState> pipeline128, int K, int max_output);
BOOL alkhawarizm_metal_matvec_autotune_prefers_128(uint64_t nanos128,
                                                   uint64_t nanos256);
void alkhawarizm_metal_log_matvec_autotune(const char *op, int K, int N0,
                                           int N1, int N2, uint64_t nanos128,
                                           uint64_t nanos256,
                                           NSUInteger selected);

#define prefer_matvec_128 alkhawarizm_metal_prefer_matvec_128
#define use_matvec_128 alkhawarizm_metal_use_matvec_128
#define should_use_bf16_matvec_x4 alkhawarizm_metal_should_use_bf16_matvec_x4
#define should_use_bf16_matvec_x8 alkhawarizm_metal_should_use_bf16_matvec_x8
#define should_use_simdgroup_reduction                                         \
  alkhawarizm_metal_should_use_simdgroup_reduction
#define should_use_bf16_pair_simd_reduction                                    \
  alkhawarizm_metal_should_use_bf16_pair_simd_reduction
#define should_use_fused_gated_ffn_matvec                                      \
  alkhawarizm_metal_should_use_fused_gated_ffn_matvec
#define matvec_shape_key alkhawarizm_metal_matvec_shape_key
#define cached_matvec_threads alkhawarizm_metal_cached_matvec_threads
#define cache_matvec_threads alkhawarizm_metal_cache_matvec_threads
#define forced_matvec_threads alkhawarizm_metal_forced_matvec_threads
#define matvec_autotune_enabled alkhawarizm_metal_matvec_autotune_enabled
#define default_matvec_threads alkhawarizm_metal_default_matvec_threads
#define matvec_autotune_prefers_128                                            \
  alkhawarizm_metal_matvec_autotune_prefers_128
#define log_matvec_autotune alkhawarizm_metal_log_matvec_autotune

#endif
