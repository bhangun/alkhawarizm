plugins {
    java
}

dependencies {
    implementation(project(":core:alkhawarizm-gguf-converter"))
    implementation(project(":core:alkhawarizm-gguf-core"))
    val gollekCore = findProject(":core:gollek-core")
    if (gollekCore != null) {
        implementation(gollekCore)
    } else {
        implementation("tech.kayys.gollek:gollek-core:0.1.0-SNAPSHOT")
    }
    implementation("tech.kayys.alkhawarizm:alkhawarizm-tensor:0.1.0-SNAPSHOT")
    val gollekSpi = findProject(":spi:gollek-spi")
    if (gollekSpi != null) {
        implementation(gollekSpi)
    } else {
        implementation("tech.kayys.gollek:gollek-spi:0.1.0-SNAPSHOT")
    }
    implementation(project(":core:alkhawarizm-safetensor-loader"))
    implementation("io.smallrye.reactive:mutiny:2.5.5")
    implementation("com.google.code.gson:gson:2.11.0")
    implementation("com.fasterxml.jackson.core:jackson-annotations:2.16.1")
    implementation("com.fasterxml.jackson.core:jackson-databind:2.16.1")
    implementation("jakarta.enterprise:jakarta.enterprise.cdi-api")
    implementation("jakarta.inject:jakarta.inject-api")
    implementation("org.slf4j:slf4j-api:2.0.13")
}
