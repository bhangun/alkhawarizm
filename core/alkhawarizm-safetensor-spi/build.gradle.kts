plugins {
    java
}

dependencies {
    val gollekTokenizer = findProject(":core:gollek-tokenizer-core")
    if (gollekTokenizer != null) {
        implementation(gollekTokenizer)
    } else {
        implementation("tech.kayys.gollek:gollek-tokenizer-core:0.1.0-SNAPSHOT")
    }

    val gollekMultimodal = findProject(":spi:gollek-spi-multimodal")
    if (gollekMultimodal != null) {
        implementation(gollekMultimodal)
    } else {
        implementation("tech.kayys.gollek:gollek-spi-multimodal:0.1.0-SNAPSHOT")
    }

    val gollekInference = findProject(":spi:gollek-spi-inference")
    if (gollekInference != null) {
        implementation(gollekInference)
    } else {
        implementation("tech.kayys.gollek:gollek-spi-inference:0.1.0-SNAPSHOT")
    }

    implementation("tech.kayys.alkhawarizm:alkhawarizm-spi-model:0.1.0-SNAPSHOT")
}
