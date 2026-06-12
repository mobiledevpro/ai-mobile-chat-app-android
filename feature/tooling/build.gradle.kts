plugins {
    id("core-module")
}

dependencies {
    implementation(projects.core.coroutines)
    implementation(libs.jsoup)
}