rootProject.name = "alkhawarizm-engine"

// ── Composite build: tafkir (quantizer modules) ──────────────────────────────
// Include tafkir as a composite build when the directory is present locally or
// in CI. When tafkir is available as a composite build, Gradle substitutes the
// tech.kayys.tafkir:* Maven coordinates with source-project dependencies, so
// the SNAPSHOT JARs never need to be downloaded from a remote repository.
val tafkirDir = file("../tafkir")
if (tafkirDir.exists() &&
    (tafkirDir.resolve("settings.gradle.kts").isFile ||
     tafkirDir.resolve("settings.gradle").isFile)) {
    includeBuild("../tafkir")
}
// ─────────────────────────────────────────────────────────────────────────────

fun includeOptionalProject(projectPath: String, vararg candidatePaths: String) {
    val projectDir = candidatePaths
        .map { file(it) }
        .firstOrNull { candidate ->
            candidate.resolve("build.gradle.kts").isFile || candidate.resolve("build.gradle").isFile
        }
        ?: return

    include(projectPath)
    project(":$projectPath").projectDir = projectDir
}

includeOptionalProject("core:alkhawarizm-core", "core/alkhawarizm-core")

// Autograd is training-only; exclude from foundational builds
val skipAutograd = true
if (!skipAutograd) {
    include("core:autograd")
}

include("core:alkhawarizm-tensor")
include("core:alkhawarizm-core")
include("core:alkhawarizm-error-code")
include("core:alkhawarizm-nn")

include("core:alkhawarizm-spi-model")
// New SPI modules — formalises Alkhawarizm as the inference/serving framework layer
// These will host contracts elevated from Gollek (Phase 2 source migration)
include("core:alkhawarizm-spi-inference")
include("core:alkhawarizm-spi-plugin")
include("core:alkhawarizm-spi-tokenizer")
include("core:alkhawarizm-3d")

//include("backend:blackwell:alkhawarizm-kernel-blackwell")

include("backend:cpu:alkhawarizm-backend-cpu")
include("backend:cuda:alkhawarizm-backend-cuda")
include("backend:cuda:alkhawarizm-kernel-cuda")
//include("backend:cuda:alkhawarizm-plugin-kernel-cuda")
//include("backend:directml:alkhawarizm-plugin-kernel-directml")
val skipHat = gradle.startParameter.projectProperties["skipHat"] == "true"
if (!skipHat) {
    include("backend:hat:alkhawarizm-backend-hat")
}
include("backend:metal:alkhawarizm-backend-metal")
//include("backend:metal:alkhawarizm-mlx-binding")

//include("backend:rocm:alkhawarizm-kernel-rocm")
//include("backend:rocm:alkhawarizm-plugin-kernel-rocm")

// Dynamically include model family projects under models/
file("models")
    .listFiles { candidate ->
        candidate.isDirectory &&
                candidate.name.startsWith("alkhawarizm-model-") &&
                (candidate.resolve("build.gradle.kts").isFile || candidate.resolve("build.gradle").isFile)
    }
    ?.sortedBy { it.name }
    ?.forEach { modelProject ->
        include("models:${modelProject.name}")
        project(":models:${modelProject.name}").projectDir = modelProject
    }

// GGUF Suite: alkhawarizm-gguf-api, alkhawarizm-gguf-llamacpp, alkhawarizm-gguf-core, alkhawarizm-gguf-java
include("core:alkhawarizm-gguf-api")
include("core:alkhawarizm-gguf-llamacpp")
include("core:alkhawarizm-gguf-core")
include("core:alkhawarizm-gguf-java")

include("core:alkhawarizm-rocksdb")
include("core:alkhawarizm-helixdb")

// ── Modules with external dependencies (tafkir / SPI) ─────────────────────
// These modules require tafkir quantizer artifacts (tech.kayys.tafkir:*) which
// are resolved via composite build (../tafkir) when available, or from a local
// Maven repository. Use includeOptionalProject so they are silently skipped on
// checkouts where the module directories are absent.
includeOptionalProject("core:alkhawarizm-safetensor-api",          "core/alkhawarizm-safetensor-api")
includeOptionalProject("core:alkhawarizm-safetensor-spi",          "core/alkhawarizm-safetensor-spi")
includeOptionalProject("core:alkhawarizm-safetensor-core",         "core/alkhawarizm-safetensor-core")
includeOptionalProject("core:alkhawarizm-safetensor-loader",       "core/alkhawarizm-safetensor-loader")
includeOptionalProject("core:alkhawarizm-safetensor-quantization", "core/alkhawarizm-safetensor-quantization")
includeOptionalProject("core:alkhawarizm-gguf-bridge",             "core/alkhawarizm-gguf-bridge")
includeOptionalProject("core:alkhawarizm-gguf-fast-bridge",        "core/alkhawarizm-gguf-fast-bridge")
includeOptionalProject("core:alkhawarizm-gguf-converter",          "core/alkhawarizm-gguf-converter")
includeOptionalProject("core:alkhawarizm-gguf-converter-java",     "core/alkhawarizm-gguf-converter-java")
