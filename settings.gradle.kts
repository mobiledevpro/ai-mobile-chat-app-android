pluginManagement {
    includeBuild("build-logic")
    repositories {
        // Putting less popular or slower repositories (like the Gradle Plugin Portal) at the end can speed up dependency resolution.
        google()
        mavenCentral()
        gradlePluginPortal()
    }
}

dependencyResolutionManagement {
    repositoriesMode.set(RepositoriesMode.PREFER_SETTINGS)
    repositories {
        google()
        mavenCentral()
    }
}
rootProject.name = "ai-mobile-chat-app"

include(":app")
include(":core:ui")
include(":core:navigation")
include(":core:coroutines")
include(":core:di")
include(":core:util")
include(":core:filestore")

include(":feature:home")
include(":feature:settings")
include(":feature:chat")
include(":core:database")
