import java.util.Properties

plugins {
    id("core-module")
}

android {
    buildFeatures {
        buildConfig = true
    }

    val localProperties = Properties()
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        localProperties.load(localPropertiesFile.inputStream())
    }

    val huggingFaceToken: String = localProperties.getProperty("HUGGING_FACE_TOKEN") ?: ""

    defaultConfig {
        buildConfigField("String", "HUGGING_FACE_TOKEN", "\"$huggingFaceToken\"")
    }
}

dependencies {
    implementation(projects.core.di)
    implementation(projects.core.coroutines)
    implementation(projects.core.filestore)
    implementation(libs.workmanager)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
}