plugins {
    `kotlin-dsl`
}

repositories {
    gradlePluginPortal()
    mavenCentral()
}

kotlin {
    dependencies {
        implementation("org.jetbrains.kotlin:kotlin-gradle-plugin:2.1.21")
        implementation("com.squareup:kotlinpoet:2.2.0")
    }
}

gradlePlugin {
    plugins {
        create("cdp-generate") {
            id = "dev.kdriver.cdp"
            implementationClass = "dev.kdriver.cdp.generate.CdpGeneratePlugin"
        }
    }
}
