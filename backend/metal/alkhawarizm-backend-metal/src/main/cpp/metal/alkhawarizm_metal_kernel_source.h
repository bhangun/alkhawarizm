/**
 * alkhawarizm_metal_kernel_source.h — runtime Metal shader source builders.
 */

#ifndef ALKHAWARIZM_METAL_KERNEL_SOURCE_H
#define ALKHAWARIZM_METAL_KERNEL_SOURCE_H

#import <Foundation/Foundation.h>

NSString *alkhawarizm_metal_runtime_kernel_source(void);
NSString *alkhawarizm_metal_matvec_kernel_source(NSUInteger threads);

#endif
