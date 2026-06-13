plugins {
    id("feature-module")
}

dependencies {
    implementation(libs.mediapipe)
    implementation(libs.ai.local.agents)
    implementation(libs.documentfile)
    implementation(projects.core.filestore)
    implementation(projects.core.database)
    implementation(projects.feature.tooling)
    implementation(projects.feature.download)
}