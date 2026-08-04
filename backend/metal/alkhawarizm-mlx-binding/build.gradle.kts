plugins {
    java
}

dependencies {
    implementation(project(":runner:safetensor:alkhawarizm-safetensor-core"))
    implementation(project(":spi:alkhawarizm-spi"))
    implementation("io.quarkus:quarkus-arc")
    
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
}

tasks.test {
    useJUnitPlatform()
}

