plugins {
    id("core-module")
}

dependencies {
    implementation(projects.core.di)
    implementation(libs.coroutines.android)
    implementation(libs.androidx.core.ktx)
}