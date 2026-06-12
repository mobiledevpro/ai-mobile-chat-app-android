plugins {
    id("feature-module")
}

dependencies {
    implementation(libs.mediapipe)
    implementation(libs.documentfile)
    implementation(projects.core.filestore)
    implementation(projects.core.database)
    implementation(projects.feature.tooling)
}