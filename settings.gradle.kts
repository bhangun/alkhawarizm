rootProject.name = "alkhawarizm-engine"

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

val skipGollek = gradle.startParameter.projectProperties["skipGollek"] == "true"
if (!skipGollek) {
    includeOptionalProject("core:gollek-core", "../gollek/framework/core/gollek-core")
    includeOptionalProject("core:gollek-tokenizer-core", "../gollek/framework/core/gollek-tokenizer-core")
    includeOptionalProject("spi:gollek-spi", "../gollek/framework/spi/gollek-spi")
    includeOptionalProject("spi:gollek-spi-multimodal", "../gollek/framework/spi/gollek-spi-multimodal")
    includeOptionalProject("spi:gollek-spi-inference", "../gollek/framework/spi/gollek-spi-inference")
}

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



include("core:alkhawarizm-rocksdb")
include("core:alkhawarizm-helixdb")

include(":core:alkhawarizm-safetensor-api")
include(":core:alkhawarizm-safetensor-core")
include(":core:alkhawarizm-safetensor-loader")
include(":core:alkhawarizm-safetensor-quantization")
include(":core:alkhawarizm-safetensor-spi")
include(":core:alkhawarizm-gguf-bridge")
include(":core:alkhawarizm-gguf-fast-bridge")
include(":core:alkhawarizm-gguf-converter")
include(":core:alkhawarizm-gguf-converter-java")
include(":core:alkhawarizm-gguf-core")
