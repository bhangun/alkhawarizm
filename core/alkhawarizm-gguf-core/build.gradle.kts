plugins {
    `java-library`
}

sourceSets {
    main {
        java {
            // Keep the actively used GGUF metadata/parser/tokenizer surface.
            // The module also contains older exporter/runner/training trees that
            // do not match today's package layout and pull in stale dependencies.
            include(
                "tech/kayys/alkhawarizm/gguf/core/GGUFConstants.java",
                "tech/kayys/alkhawarizm/gguf/core/GGUFTensorInfo.java",
                "tech/kayys/alkhawarizm/gguf/core/GgmlType.java",
                "tech/kayys/alkhawarizm/gguf/core/GgufMetaType.java",
                "tech/kayys/alkhawarizm/gguf/core/GgufMetaValue.java",
                "tech/kayys/alkhawarizm/gguf/core/GgufExporter.java",
                "tech/kayys/alkhawarizm/gguf/core/GgufModel.java",
                "tech/kayys/alkhawarizm/gguf/loader/GGUFModel.java",
                "tech/kayys/alkhawarizm/gguf/loader/GGUFTensorInfo.java",
                "tech/kayys/alkhawarizm/gguf/loader/GGUFParser.java",
                "tech/kayys/alkhawarizm/gguf/loader/GGUFReader.java",
                "tech/kayys/alkhawarizm/gguf/loader/gguf/*.java",
                "tech/kayys/alkhawarizm/gguf/loader/quant/*.java",
                "tech/kayys/alkhawarizm/gguf/runtime/*.java",
                "tech/kayys/alkhawarizm/gguf/tokenizer/GGUFTokenizer.java",
                "tech/kayys/alkhawarizm/gguf/writer/GGUFWriter.java",
                "tech/kayys/alkhawarizm/gguf/model/alkhawarizm/*.java",
                "tech/kayys/alkhawarizm/gguf/model/ModelConfig.java",
                "tech/kayys/alkhawarizm/gguf/runner/AljabrWeightAdapter.java",
            )
        }
    }
}

dependencies {
    api(project(":core:alkhawarizm-nn"))
    api(project(":core:alkhawarizm-tensor"))
    implementation(project(":core:alkhawarizm-spi-model"))
    implementation("com.google.code.gson:gson:2.10.1")
    implementation(project(":core:gollek-tokenizer-core"))
    implementation("io.quarkus:quarkus-core:3.32.2")
    implementation("org.jboss.logging:jboss-logging:3.6.1.Final")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform()
}
