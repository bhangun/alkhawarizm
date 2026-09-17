plugins {
    java
}

dependencies {
    val alkhawarizmCoreProject = findProject(":core:alkhawarizm-core")
    if (alkhawarizmCoreProject != null) {
        testImplementation(alkhawarizmCoreProject)
    }
    val gollekCoreProject = findProject(":core:gollek-core")
    if (gollekCoreProject != null) {
        testImplementation(gollekCoreProject)
    }
}
