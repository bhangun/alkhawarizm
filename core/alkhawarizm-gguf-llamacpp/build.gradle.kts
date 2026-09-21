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

dependencies {
    implementation(project(":core:alkhawarizm-gguf-api"))
    implementation("tech.kayys.gollek:gollek-plugin-runner-core:0.1.0-SNAPSHOT")
    testImplementation(group = "org.junit.jupiter", name = "junit-jupiter")
    testRuntimeOnly(group = "org.junit.platform", name = "junit-platform-launcher")
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

tasks.jar {
    manifest {
        attributes(
            mapOf(
                "Plugin-Id" to "gguf-runner",
                "Plugin-Type" to "runner",
                "Plugin-Provider" to "tech.kayys.alkhawarizm.gguf.llamacppbackend.LlamaCppGgufBackendProvider",
                "Plugin-Version" to "0.1.0-SNAPSHOT",
                "Supported-Formats" to ".gguf"
            )
        )
    }
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
