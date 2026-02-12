rootProject.name = "kdriver"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

includeBuild("cdp-generate")
include(":cdp")
include(":core")
include(":opentelemetry")
