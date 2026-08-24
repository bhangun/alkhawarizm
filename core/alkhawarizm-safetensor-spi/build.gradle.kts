plugins {
    java
}

dependencies {
    implementation(project(":core:gollek-tokenizer-core"))
    implementation(project(":spi:gollek-spi-multimodal"))
    implementation(project(":spi:gollek-spi-inference"))
    implementation("tech.kayys.alkhawarizm:alkhawarizm-spi-model:0.1.0-SNAPSHOT")
}
