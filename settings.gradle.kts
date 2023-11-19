rootProject.name = "DecomposeSimplifications"

include(":core")
include(":optionalExtensions")
include(":sample:androidApp")
include(":sample:desktopApp")
include(":sample:shared")

pluginManagement {
    repositories {
        google()
        gradlePluginPortal()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}

dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}


plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version("0.4.0")
}