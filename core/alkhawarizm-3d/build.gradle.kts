plugins {
    java
}

dependencies {
    implementation(project(":core:alkhawarizm-tensor"))
    implementation(project(":core:alkhawarizm-spi-model"))

    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
}
