plugins {
    id("feature-module")
}

dependencies {
    implementation(libs.pdfbox)
    implementation(libs.mediapipe)
    implementation(libs.documentfile)
    implementation(projects.core.filestore)
    implementation(projects.core.database)
}