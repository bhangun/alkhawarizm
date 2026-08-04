/**
 * alkhawarizm_metal_runtime.h — exported runtime/device helpers for the Metal
 * dylib.
 */

#ifndef ALKHAWARIZM_METAL_RUNTIME_H
#define ALKHAWARIZM_METAL_RUNTIME_H

#include <stddef.h>

#ifdef __cplusplus
extern "C" {
#endif

int alkhawarizm_metal_init(void);
long alkhawarizm_metal_available_memory(void);

int alkhawarizm_metal_set_mps_matvec_enabled(int enabled);
int alkhawarizm_metal_set_mps_matvec_autotune_enabled(int enabled);
int alkhawarizm_metal_set_mps_matvec_max_inner(int max_inner);
int alkhawarizm_metal_set_mps_matvec_max_output(int max_output);
int alkhawarizm_metal_set_mps_matvec_autotune_max_output(int max_output);

void *alkhawarizm_metal_alloc(size_t bytes, size_t align);

int alkhawarizm_metal_argmax_f32(const void *logits, int n, int reject0,
                                 int reject1, int reject2, int reject3,
                                 int reject4, int reject5, int reject6,
                                 int reject7);

int alkhawarizm_metal_device_name(char *buf, int bufSz);
int alkhawarizm_metal_is_unified_memory(void);

#ifdef __cplusplus
}
#endif

#endif
