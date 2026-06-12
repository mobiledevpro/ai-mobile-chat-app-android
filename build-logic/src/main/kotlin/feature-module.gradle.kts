plugins {
    id("core-module")
}

with(pluginManager) {
    apply("org.jetbrains.kotlin.plugin.compose")
}

android {
    buildFeatures.compose = true
}

dependencies {
    implementation(project(":core:ui"))
    implementation(project(":core:di"))
    implementation(project(":core:coroutines"))
    implementation(project(":core:util"))

    implementation(libs.bundle("lifecycle"))
    implementation(libs.library("activity.ktx"))
}