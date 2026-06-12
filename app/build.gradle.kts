import java.io.FileNotFoundException
import java.util.Properties

@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.mobiledevpro.app"
    compileSdk = libs.versions.sdk.compile.get().toInt()


    defaultConfig {
        applicationId = "com.mobiledevpro.aichat.app"
        minSdk = libs.versions.sdk.min.get().toInt()
        targetSdk = libs.versions.sdk.target.get().toInt()
        versionCode = libs.versions.app.version.code.get().toInt()
        versionName = libs.versions.app.version.name.get()

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        val keystoreProperties = Properties()
        try {
            keystoreProperties.load(File(rootDir, "keystore.properties").inputStream())

            create("release") {
                storeFile = file("release.jks")
                storePassword = keystoreProperties["KSTOREPWD"] as String
                keyAlias = keystoreProperties["KEYSTORE_ALIAS"] as String
                keyPassword = keystoreProperties["KEYPWD"] as String
            }
        } catch (e: FileNotFoundException) {
            println("Warning: keystore.properties file not found.")
        }
    }

    buildTypes {
        getByName("debug") {
            isDebuggable = true
        }

        getByName("release") {
            isDebuggable = false
            isMinifyEnabled = true
            isShrinkResources = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
            signingConfig = try {
                signingConfigs.getByName("release")
            } catch (e: UnknownDomainObjectException) {
                println("SigningConfig with not found. Skipping...")
                null
            }
        }
    }

    flavorDimensions += listOf("default")

    productFlavors {
        create("production") {
            dimension = "default"
        }
    }

    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }

    kotlin {
        jvmToolchain(17)
    }


    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging {
        resources.excludes.add("/META-INF/{AL2.0,LGPL2.1}")
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    coreLibraryDesugaring(libs.desugaring)
    implementation(libs.bundles.lifecycle)

    implementation(projects.core.navigation)
    implementation(projects.core.filestore)
    implementation(projects.core.database)

    implementation(libs.pdfbox)
}