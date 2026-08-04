plugins {
    `java-library`
    `maven-publish`
}

group = "tech.kayys.alkhawarizm"
version = "0.1.0-SNAPSHOT"

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
    mavenLocal()
}

sourceSets {
    named("main") {
        java {
            setSrcDirs(listOf("src/main/java"))
            include("tech/kayys/alkhawarizm/cuda/binding/CudaBinding.java")
            include("tech/kayys/alkhawarizm/cuda/binding/CudaCpuFallback.java")
            include("tech/kayys/alkhawarizm/cuda/detection/CudaDetector.java")
            include("tech/kayys/alkhawarizm/cuda/detection/CudaCapabilities.java")
            include("tech/kayys/alkhawarizm/cuda/gpu/GPUMemoryPool.java")
            include("tech/kayys/alkhawarizm/cuda/gpu/GPUAccelerator.java")
            include("tech/kayys/alkhawarizm/cuda/gpu/CUDAStreamManager.java")
            include("tech/kayys/alkhawarizm/cuda/config/CudaRunnerMode.java")
        }
    }
}

dependencies {
    //implementation(project(":spi:alkhawarizm-spi-provider"))

    //implementation(project(":core:plugin:alkhawarizm-plugin-runner-core"))
   // implementation(group = "tech.kayys.alkhawarizm", name = "alkhawarizm-engine")
   // implementation(project(":optimization:alkhawarizm-plugin-kv-cache"))
    implementation(project(":core:alkhawarizm-tensor"))
   /*  implementation(project(":optimization:alkhawarizm-plugin-fa4"))
    implementation(project(":optimization:alkhawarizm-plugin-fa3")) */
    implementation(group = "io.quarkus", name = "quarkus-arc")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter")
    testImplementation(group = "org.assertj", name = "assertj-core")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    repositories {
        mavenLocal()
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
