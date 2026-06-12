@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.android.library) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.kapt) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.compose.compiler) apply false
}

tasks.register("clean", Delete::class) {
    delete(layout.buildDirectory.get())
}

// To generate the Compose compiler metrics. Files generated in the /build/compose_metrics directory
// Article https://proandroiddev.com/optimize-app-performance-by-mastering-stability-in-jetpack-compose-69f40a8c785d

// Interpreting Compose Compiler Metrics https://github.com/JetBrains/kotlin/blob/master/plugins/compose/design/compiler-metrics.md
subprojects {
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        compilerOptions.freeCompilerArgs.addAll(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:reportsDestination=" +
                    project.layout.buildDirectory.get().asFile.absolutePath + "/compose_metrics"
        )
        compilerOptions.freeCompilerArgs.addAll(
            "-P",
            "plugin:androidx.compose.compiler.plugins.kotlin:metricsDestination=" +
                    project.layout.buildDirectory.get().asFile.absolutePath + "/compose_metrics"
        )
    }
}