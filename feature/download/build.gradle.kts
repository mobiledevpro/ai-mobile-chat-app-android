plugins {
    id("core-module")
}

dependencies {
    implementation(projects.core.di)
    implementation(projects.core.coroutines)
    implementation(libs.workmanager)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}