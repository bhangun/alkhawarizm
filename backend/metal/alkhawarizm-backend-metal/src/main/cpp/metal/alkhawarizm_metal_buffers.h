/**
 * alkhawarizm_metal_buffers.h — shared Metal buffer helpers.
 */

#ifndef ALKHAWARIZM_METAL_BUFFERS_H
#define ALKHAWARIZM_METAL_BUFFERS_H

#import <Foundation/Foundation.h>
#import <Metal/Metal.h>
#include <stddef.h>

id<MTLBuffer> alkhawarizm_metal_wrap_ptr(void *ptr, size_t bytes);
id<MTLBuffer> alkhawarizm_metal_wrap_weight_ptr(const void *ptr, size_t bytes);

BOOL alkhawarizm_metal_ensure_swiglu_scratch(size_t activation_bytes,
                                             id<MTLBuffer> *gate,
                                             id<MTLBuffer> *up,
                                             id<MTLBuffer> *combined);

BOOL alkhawarizm_metal_ensure_swiglu_combined_scratch(size_t activation_bytes,
                                                      id<MTLBuffer> *combined);

#define wrap_ptr alkhawarizm_metal_wrap_ptr
#define wrap_weight_ptr alkhawarizm_metal_wrap_weight_ptr
#define ensure_swiglu_scratch alkhawarizm_metal_ensure_swiglu_scratch
#define ensure_swiglu_combined_scratch                                         \
  alkhawarizm_metal_ensure_swiglu_combined_scratch

#endif
