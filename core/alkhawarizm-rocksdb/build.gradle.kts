plugins {
    `java-library`
}

dependencies {
    api(project(":core:alkhawarizm-core"))
    implementation("org.rocksdb:rocksdbjni:9.2.1")
}
