plugins {
    java
}

dependencies {
    implementation(project(":core:alkhawarizm-tokenizer-core"))
    implementation(project(":spi:alkhawarizm-spi-multimodal"))
    implementation(project(":spi:alkhawarizm-spi-inference"))
    implementation("tech.kayys.alkhawarizm:alkhawarizm-spi-model:0.1.0-SNAPSHOT")
}
