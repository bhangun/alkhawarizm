plugins {
    java
}

dependencies {
    val alkhawarizmCoreProject = findProject(":core:alkhawarizm-core")
    if (alkhawarizmCoreProject != null) {
        testImplementation(alkhawarizmCoreProject)
    }
}
