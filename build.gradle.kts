plugins {
    kotlin("multiplatform") version "1.5.10"
    kotlin("plugin.serialization") version "1.5.10"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

kotlin {

    jvm()

    js(IR) {
        browser {
            webpackTask {
                cssSupport.enabled = true
            }

            runTask {
                cssSupport.enabled = true
            }

            testTask {
                useKarma {
                    useChromeHeadless()
                    webpackConfig.cssSupport.enabled = true
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(kotlin("stdlib-common"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.5.0")
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.0") {
                    exclude("org.jetbrains.kotlinx", "kotlinx-serialization-core")
                }
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jsMain by getting {
            dependencies {
                implementation(kotlin("stdlib-js"))
                implementation("org.jetbrains.kotlinx:kotlinx-html-js:0.7.3")
            }
        }
        val jsTest by getting {
            dependencies {
                implementation(kotlin("test-js"))
            }
        }
        val jvmMain by getting {
            dependencies {
//                implementation("org.slf4j:slf4j-simple:1.7.30")
            }
        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit5"))
                implementation("org.junit.platform:junit-platform-console:1.5.2")
                implementation("org.junit.jupiter:junit-jupiter-params:5.5.2")
                runtimeOnly("org.junit.jupiter:junit-jupiter-engine:5.5.2")
            }
        }

        all {
            languageSettings.apply {
                languageVersion = "1.5"
                apiVersion = "1.5"
                useExperimentalAnnotation("kotlin.time.ExperimentalTime")
                useExperimentalAnnotation("kotlin.js.ExperimentalJsExport")
            }
        }
    }

    tasks.getByName("jvmTest", Test::class) {
        useJUnitPlatform()
    }

}
