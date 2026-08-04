plugins {
    java
}

dependencies {
    implementation(project(":core:alkhawarizm-tensor"))
    implementation(project(":backend:cpu:alkhawarizm-backend-cpu"))
    implementation(project(":backend:cuda:alkhawarizm-kernel-cuda"))
    implementation("org.jboss.logging:jboss-logging:3.6.1.Final")
}
