/**
 * alkhawarizm_metal_cpu_fallback.h — CPU fallback kernels for the Metal bridge.
 */

#ifndef ALKHAWARIZM_METAL_CPU_FALLBACK_H
#define ALKHAWARIZM_METAL_CPU_FALLBACK_H

int alkhawarizm_metal_cpu_add(void *C, const void *A, const void *B, int N);
int alkhawarizm_metal_cpu_matmul_nn(void *C, const void *A, const void *B,
                                    int M, int K, int N, float alpha,
                                    float beta);
int alkhawarizm_metal_cpu_matmul_tb(void *C, const void *A, const void *B,
                                    int M, int K, int N, float alpha,
                                    float beta);
int alkhawarizm_metal_cpu_matvec_rows(void *y, const void *A, const void *x,
                                      int rows, int cols, int lda, float alpha,
                                      float beta);
int alkhawarizm_metal_cpu_matvec_cols(void *y, const void *A, const void *x,
                                      int rows, int cols, int lda, float alpha,
                                      float beta);
int alkhawarizm_metal_cpu_rmsnorm(void *out, const void *x, const void *weight,
                                  int N, float eps, int addOne);
int alkhawarizm_metal_cpu_silu_ffn(void *out, const void *gate, const void *up,
                                   int N);
int alkhawarizm_metal_cpu_gelu_ffn(void *out, const void *gate, const void *up,
                                   int N);
int alkhawarizm_metal_cpu_layernorm(void *out, const void *x,
                                    const void *weight, const void *bias, int N,
                                    float eps);
int alkhawarizm_metal_cpu_silu(void *out, const void *x, int N);
int alkhawarizm_metal_cpu_gelu(void *out, const void *x, int N);

#endif
