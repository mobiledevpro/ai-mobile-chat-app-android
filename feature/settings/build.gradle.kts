plugins {
    id("feature-module")
}

val appVersionName: String = libs.versions.app.version.name.get()
val appVersionCode: String = libs.versions.app.version.code.get()

android.buildFeatures.buildConfig = true
android.defaultConfig {
    buildConfigField("String", "VERSION_NAME", "\"$appVersionName\"")
    buildConfigField("String", "VERSION_CODE", "\"$appVersionCode\"")
}


dependencies {
}
