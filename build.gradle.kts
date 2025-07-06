plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.maven) apply false
    alias(libs.plugins.dokka)
}

allprojects {
    group = "dev.kdriver"
    version = "0.2.1"

    repositories {
        mavenCentral()
    }
}
