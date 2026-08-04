/**
 * alkhawarizm_metal_mps_validation.h — MPS matvec validation helpers.
 */

#ifndef ALKHAWARIZM_METAL_MPS_VALIDATION_H
#define ALKHAWARIZM_METAL_MPS_VALIDATION_H

#import <Foundation/Foundation.h>
#include <stdint.h>

NSString *alkhawarizm_metal_mps_matvec_shape_key(int K, int N);
float alkhawarizm_metal_bf16_to_f32(uint16_t bits);
uint16_t alkhawarizm_metal_f32_to_bf16_bits(float value);

BOOL alkhawarizm_metal_validate_mps_matvec_half_output(const float *C,
                                                       const float *A,
                                                       const uint16_t *B, int K,
                                                       int N);

BOOL alkhawarizm_metal_validate_mps_matvec_bf16_output(const float *C,
                                                       const float *A,
                                                       const uint16_t *B, int K,
                                                       int N);

#define mps_matvec_shape_key alkhawarizm_metal_mps_matvec_shape_key
#define bf16_to_f32_bridge alkhawarizm_metal_bf16_to_f32
#define f32_to_bf16_bits_bridge alkhawarizm_metal_f32_to_bf16_bits
#define validate_mps_matvec_half_output                                        \
  alkhawarizm_metal_validate_mps_matvec_half_output
#define validate_mps_matvec_bf16_output                                        \
  alkhawarizm_metal_validate_mps_matvec_bf16_output

#endif
