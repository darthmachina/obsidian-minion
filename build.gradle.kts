plugins {
    kotlin("js") version "1.9.22"
    id("org.jetbrains.kotlin.plugin.serialization") version "1.9.22"
    id("io.kotest.multiplatform") version "5.8.0"
}

group = "app.minion"
version = "0.14.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

dependencies {
    implementation(npm("obsidian", "0.12.17"))

    val kotlinVersion = "1.9.22"
    val kotlinxHtmlVersion = "0.11.0"
    val kvisionVersion = "7.3.1"
    val arrowVersion = "1.2.1"
    val serializationVersion = "1.6.3"
    val loggingVersion = "3.0.5"
    val datetimeVersion = "0.5.0"
    val coroutinesCoreVersion = "1.8.0"
    val kotestVersion = "5.8.0"
    val kotestAssertionsArrowVersion = "1.4.0"
    val yamlktVersion = "0.13.0"
    val uuidVersion = "0.0.22"

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesCoreVersion")
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("net.mamoe.yamlkt:yamlkt:$yamlktVersion")
    implementation("io.github.microutils:kotlin-logging:$loggingVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")
    implementation(npm("@js-joda/timezone", "2.3.0"))
    implementation("app.softwork:kotlinx-uuid-core:$uuidVersion")

    implementation("io.kvision:kvision:$kvisionVersion")
    implementation("io.kvision:kvision-fontawesome:$kvisionVersion")
    implementation("io.kvision:kvision-state:$kvisionVersion")
    implementation("io.kvision:kvision-redux-kotlin:$kvisionVersion")

    implementation("org.jetbrains.kotlinx:kotlinx-html-js:$kotlinxHtmlVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-html:$kotlinxHtmlVersion")

    testImplementation("io.kotest:kotest-assertions-core:$kotestVersion")
    testImplementation("io.kotest:kotest-framework-engine:$kotestVersion")
    testImplementation("io.kotest.extensions:kotest-assertions-arrow:$kotestAssertionsArrowVersion")
    testImplementation("io.mockk:mockk-js:1.7.17")
}

kotlin {
    js(IR) {
        binaries.executable()
        browser {
            useCommonJs()
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
            }
            webpackTask {
                output.libraryTarget = "commonjs"
                output.library = null
                outputFileName = "main.js"
            }
            testTask {
                useKarma {
                    useChromiumHeadless()
                }
            }
        }
    }
}

// OptIn to JsExport annotation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
}
