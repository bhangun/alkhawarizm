plugins {
    java
}

dependencies {
    // MLX binding: bridges Apple MLX tensors into the Alkhawarizm ComputeBackend contract
    implementation(project(":core:alkhawarizm-tensor"))
    implementation(project(":core:alkhawarizm-safetensor-core"))
    implementation("io.quarkus:quarkus-arc")

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

