pluginManagement {
    repositories {
        mavenCentral()
        gradlePluginPortal()
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.5.0"
}

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            // Plugins
            version("kotlin", "2.1.21")
            plugin("multiplatform", "org.jetbrains.kotlin.multiplatform").versionRef("kotlin")
            plugin("serialization", "org.jetbrains.kotlin.plugin.serialization").versionRef("kotlin")
            plugin("kover", "org.jetbrains.kotlinx.kover").version("0.8.3")
            plugin("detekt", "io.gitlab.arturbosch.detekt").version("1.23.8")
            plugin("dokka", "org.jetbrains.dokka").version("2.0.0")
            plugin("ksp", "com.google.devtools.ksp").version("2.1.21-2.0.2")
            plugin("maven", "com.vanniktech.maven.publish").version("0.30.0")

            // Kaccelero
            version("kaccelero", "0.6.6")
            library("kaccelero-core", "dev.kaccelero", "core").versionRef("kaccelero")

            // Ktor
            version("ktor", "3.1.3")
            library("ktor-serialization-kotlinx-json", "io.ktor", "ktor-serialization-kotlinx-json").versionRef("ktor")
            library("ktor-client-websockets", "io.ktor", "ktor-client-websockets").versionRef("ktor")
            library("ktor-client-content-negotiation", "io.ktor", "ktor-client-content-negotiation").versionRef("ktor")
            library("ktor-client-apache", "io.ktor", "ktor-client-apache").versionRef("ktor")
            library("ktor-client-cio", "io.ktor", "ktor-client-cio").versionRef("ktor")
            library("ktor-client-js", "io.ktor", "ktor-client-js").versionRef("ktor")
            library("ktor-client-darwin", "io.ktor", "ktor-client-darwin").versionRef("ktor")

            // Tests
            library("tests-mockk", "io.mockk:mockk:1.13.12")
            library("tests-jsoup", "org.jsoup:jsoup:1.16.2")
            library("tests-coroutines", "org.jetbrains.kotlinx:kotlinx-coroutines-test:1.9.0")

            // Others
            library("kotlinx-io", "org.jetbrains.kotlinx:kotlinx-io-core:0.7.0")
            library("zstd", "com.github.luben:zstd-jni:1.5.7-4")
        }
    }
}

rootProject.name = "kdriver"
includeBuild("cdp-generate")
include(":cdp")
include(":core")
