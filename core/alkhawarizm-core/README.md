alkhawarizm-core

Purpose
- Aggregator module exposing stable public APIs from core alkhawarizm modules for external consumers (e.g., alkhawarizm)

Contents
- Re-exports: tensor, tokenizer-core, model-runner, model-repository, SPIs
- Compile-only references to quantizers so consumers can opt-in to implementations

Migration notes
- To migrate a alkhawarizm consumer, add a dependency on project(":core:alkhawarizm-core") and refactor uses of internal alkhawarizm modules to the public APIs.
- Keep only minimal runtime interfaces in alkhawarizm to avoid circular deps.

Next steps
1. Update alkhawarizm consumer build files to depend on alkhawarizm-core.
2. Extract any shared classes that cause cycles into small SPI modules if needed.
