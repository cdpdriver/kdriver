plugins {
    alias(libs.plugins.multiplatform) apply false
    alias(libs.plugins.maven) apply false
}

allprojects {
    group = "dev.kdriver"
    version = "0.1.4"

    repositories {
        mavenCentral()
    }
}
