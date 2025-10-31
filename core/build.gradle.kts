plugins {
    alias(libs.plugins.multiplatform)
    alias(libs.plugins.serialization)
    alias(libs.plugins.kover)
    alias(libs.plugins.detekt)
    alias(libs.plugins.dokka)
    alias(libs.plugins.ksp)
    alias(libs.plugins.maven)
}

mavenPublishing {
    publishToMavenCentral(com.vanniktech.maven.publish.SonatypeHost.CENTRAL_PORTAL)
    signAllPublications()
    pom {
        name.set("core")
        description.set("core of kdriver.")
        url.set(project.ext.get("url")?.toString())
        licenses {
            license {
                name.set(project.ext.get("license.name")?.toString())
                url.set(project.ext.get("license.url")?.toString())
            }
        }
        developers {
            developer {
                id.set(project.ext.get("developer.id")?.toString())
                name.set(project.ext.get("developer.name")?.toString())
                email.set(project.ext.get("developer.email")?.toString())
                url.set(project.ext.get("developer.url")?.toString())
            }
        }
        scm {
            url.set(project.ext.get("scm.url")?.toString())
        }
    }
}

kotlin {
    // Native targets
    macosX64()
    macosArm64()
    linuxX64()
    linuxArm64()
    mingwX64()

    // jvm & js
    jvmToolchain(21)
    jvm {
        testRuns.named("test") {
            executionTask.configure {
                useJUnitPlatform()
            }
        }
    }
    js {
        generateTypeScriptDefinitions()
        binaries.library()
        nodejs()
        browser()
    }

    applyDefaultHierarchyTemplate()
    sourceSets {
        all {
            languageSettings.apply {
                optIn("dev.kdriver.cdp.InternalCdpApi")
                optIn("kotlin.js.ExperimentalJsExport")
            }
        }
        val commonMain by getting {
            dependencies {
                api(project(":cdp"))
                api(libs.ktor.serialization.kotlinx.json)
                api(libs.ktor.client.content.negotiation)
                api(libs.kotlinx.io)
            }
        }
        val jvmMain by getting {
            dependencies {
                api(libs.ktor.client.apache)
                api(libs.ktor.client.cio)
                api(libs.zstd)
            }
        }
        val jsMain by getting {
            dependencies {
                api(libs.ktor.client.js)
            }
        }
        val appleMain by getting {
            dependencies {
                api(libs.ktor.client.darwin)
            }
        }
        val posixMain by creating {
            dependsOn(commonMain)
        }
        val linuxMain by getting {
            dependsOn(posixMain)
            dependencies {
                api(libs.ktor.client.curl)
            }
        }
        val macosMain by getting {
            dependsOn(posixMain)
        }
        val mingwMain by getting {
            dependencies {
                api(libs.ktor.client.winhttp)
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(libs.tests.mockk)
            }
        }
        val macosTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val linuxTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
        val mingwTest by getting {
            dependencies {
                implementation(kotlin("test"))
            }
        }
    }
}

detekt {
    buildUponDefaultConfig = true
    config.setFrom("${rootProject.projectDir}/detekt.yml")
    source.from(file("src/commonMain/kotlin"))
}
