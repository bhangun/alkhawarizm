# Alkhawarizm Architecture

## Vision: A General-Purpose ML Framework

Alkhawarizm is the **foundational ML framework** for the Wayang platform — analogous to PyTorch in the Python ecosystem. It provides everything an inference/serving engine needs to be built on top of it, with zero engine-specific assumptions.

```
┌───────────────────────────────────────────────────────┐
│                    Wayang Platform                    │ ← Agent orchestration, RAG, MCP
├───────────────────────────────────────────────────────┤
│           Aqli / Any Future Serving Engine          │ ← Pure runtime: runners, repos, SDK
├───────────────────────────────────────────────────────┤
│                   Alkhawarizm SPI                     │ ← Inference + serving contracts
│  (ModelRunner, InferenceRequest, PluginSPI, ...)      │
├───────────────────────────────────────────────────────┤
│                Alkhawarizm Core                       │ ← Tensors, ops, data types, errors
├───────────────────────────────────────────────────────┤
│              Alkhawarizm Backends                     │ ← CPU / CUDA / Metal / ROCm / HAT
└───────────────────────────────────────────────────────┘
```

The key design rule: **any compliant serving engine** (Aqli, or a future hypothetical engine) should be implementable using only Alkhawarizm contracts, without touching engine-specific code.

---

## Module Groups

### `core/` — Primitives & Contracts

| Module | Purpose | Nature |
|---|---|---|
| `alkhawarizm-tensor` | `Tensor`, `DefaultTensor`, `Shape`, `DType`, `DeviceType`, `TensorOps` | Foundation |
| `alkhawarizm-core` | Root API surface, exposes tensor + spi-model | API umbrella |
| `alkhawarizm-error-code` | `ErrorCode` enum, error taxonomy | Foundation |
| `alkhawarizm-nn` | Neural network layer ops (linear, softmax, attention) | Foundation |
| `alkhawarizm-spi-model` | **Model-level contracts**: `ModelConfig`, `ModelArchitecture`, `ModelRunner`, `MultimodalRequest/Response`, `ModelRegistry`, `ModelRepository`, `RunnerMetadata` | SPI |
| `alkhawarizm-gguf-api` | GGUF format API/contracts | SPI |
| `alkhawarizm-gguf-core` | GGUF parsing implementation | Runtime |
| `alkhawarizm-gguf-converter` | GGUF format converter | Runtime |
| `alkhawarizm-safetensor-api` | Safetensor format contracts | SPI |
| `alkhawarizm-safetensor-spi` | Safetensor loader SPI | SPI |
| `alkhawarizm-safetensor-core` | Safetensor loading implementation | Runtime |
| `alkhawarizm-rocksdb` | RocksDB KV store binding | Runtime |
| `alkhawarizm-helixdb` | HelixDB vector store binding | Runtime |
| `alkhawarizm-3d` | 3D tensor/rendering ops | Optional |
| `autograd` | Autograd engine (training only — excluded from inference builds) | Training |

### `backend/` — Hardware Compute Backends

All backends implement `ComputeBackend` from `alkhawarizm-tensor`.

| Module | Hardware | Nature |
|---|---|---|
| `backend/cpu/alkhawarizm-backend-cpu` | CPU (Java Vector API) | Runtime |
| `backend/cuda/alkhawarizm-backend-cuda` | NVIDIA CUDA | Runtime |
| `backend/cuda/alkhawarizm-kernel-cuda` | CUDA kernel implementations | Runtime |
| `backend/metal/alkhawarizm-backend-metal` | Apple Metal (macOS/iOS) | Runtime |
| `backend/hat/alkhawarizm-backend-hat` | Oracle Project Babylon HAT | Experimental |
| `backend/rocm/alkhawarizm-kernel-rocm` | AMD ROCm | Runtime |

**Backend boundary rule**: backends depend only on `core/alkhawarizm-tensor` and `core/alkhawarizm-error-code`. They must never depend on each other.

### `models/` — Model Family Definitions

Pre-built model graph descriptors auto-discovered at build time. Each `alkhawarizm-model-<name>` provides `ModelFamilyDescriptor` + `ModelFamilyPlugin` registrations consumed by `alkhawarizm-spi-model`.

---

## The SPI Elevation Plan

Alkhawarizm currently hosts model-level contracts in `alkhawarizm-spi-model`. The next step is to **host all general inference/serving SPIs** here, so any engine (Aqli or otherwise) depends only on Alkhawarizm.

### Contracts to be elevated from Aqli → Alkhawarizm

| Contract | Current location | Target Alkhawarizm module |
|---|---|---|
| `InferenceRequest`, `InferenceResponse`, `StreamingResponse` | `aqli-spi-inference` | `alkhawarizm-spi-inference` (new) |
| `InferenceEngine`, `InferencePipeline`, `InferencePhase` | `aqli-spi-inference` | `alkhawarizm-spi-inference` (new) |
| `BatchScheduler`, `BatchConfig`, `BatchStrategy` | `aqli-spi-inference` | `alkhawarizm-spi-inference` (new) |
| `EmbeddingRequest`, `EmbeddingResponse` | `aqli-spi-inference` | `alkhawarizm-spi-inference` (new) |
| `ModelRunner` (interface) | `aqli-spi-runner` → `alkhawarizm-spi-model` | Already partially there |
| `RunnerStats`, `RunnerCapabilities`, `RunnerConfiguration` | `aqli-spi-runner` | `alkhawarizm-spi-model` extension |
| `AqliPlugin` → `AlkhawarizmPlugin` | `aqli-spi-plugin` | `alkhawarizm-spi-plugin` (new) |
| `PluginHealth`, `PluginState`, `PluginRegistry` | `aqli-spi-plugin` | `alkhawarizm-spi-plugin` (new) |
| `Tokenizer`, `TokenizerPool`, `PreTokenizer` SPI | `aqli-tokenizer-core/spi/` | `alkhawarizm-spi-tokenizer` (new) |

### Target module layout after elevation

```
alkhawarizm/
  core/
    alkhawarizm-tensor/        ← tensors (unchanged)
    alkhawarizm-spi-model/     ← model configs, ModelRunner, multimodal (existing, extended)
    alkhawarizm-spi-inference/ ← NEW: InferenceRequest/Response, Engine, Pipeline, Batch, Embedding
    alkhawarizm-spi-plugin/    ← NEW: Plugin lifecycle SPI
    alkhawarizm-spi-tokenizer/ ← NEW: Tokenizer contracts
    alkhawarizm-error-code/    ← errors (unchanged)
    ...
  backend/                     ← unchanged
  models/                      ← unchanged
```

---

## Module Boundary Rules

### Core → Backend direction only
```
core/alkhawarizm-tensor   ←── backend/cpu
                          ←── backend/cuda
                          ←── backend/metal
```
Backends depend on core. Core never depends on backends.

### SPIs are pure contracts
SPI modules must contain only interfaces, abstract classes, records, enums, and annotations. No I/O, no JNI, no `new ConcreteImpl()`.

### No cross-backend dependencies
```
backend/cpu  ✗→  backend/cuda   (forbidden)
backend/cuda ✗→  backend/metal  (forbidden)
```

### Published coordinates
All `core/` and `backend/` modules must declare:
```kotlin
group = "tech.kayys.alkhawarizm"
version = "0.1.0-SNAPSHOT"
```

---

## Enforcement Checklist

Run `scripts/check-boundaries.sh` before merging PRs.

```bash
# 1. No backend module depends on another backend
grep -rn 'project(":backend:' --include="*.kts" backend/
# Expect: no output

# 2. No core module declares a backend package
grep -rn '^package tech\.kayys\.alkhawarizm\.backend' --include="*.java" core/
# Expect: no output

# 3. No stale references to deleted modules
grep -rn 'alkhawarizm-spi-provider\|alkhawarizm-model-runner\|"alkhawarizm-engine"' --include="*.kts" .
# Expect: no output

# 4. SPI modules contain no concrete implementations
grep -rn '^public class [A-Z].*implements\|^public class [A-Z].*extends' \
  core/alkhawarizm-spi-*/src --include="*.java" | grep -v 'Abstract\|Exception\|Error'
# Review any output: should only be abstract base classes or exceptions
```

---

## Change Guide

| What you want to add | Where it goes |
|---|---|
| New tensor primitive or op | `core/alkhawarizm-tensor` |
| New model config field | `core/alkhawarizm-spi-model` |
| New inference contract (request/response shape) | `core/alkhawarizm-spi-inference` (new) |
| New hardware compute backend | `backend/<vendor>/alkhawarizm-backend-<vendor>` |
| New model format (e.g. GGML) | `core/alkhawarizm-<format>-api` (SPI) + `core/alkhawarizm-<format>-core` (impl) |
| New model family (e.g. Falcon) | `models/alkhawarizm-model-falcon` |
| New tokenizer SPI | `core/alkhawarizm-spi-tokenizer` (new) |
| New tokenizer implementation | Lives in Aqli `runtime/` or a future `alkhawarizm-tokenizer-impls` module |
