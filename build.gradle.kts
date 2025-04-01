plugins {
    kotlin("js") version "2.1.10"
    id("org.jetbrains.kotlin.plugin.serialization") version "2.1.10"
    id("io.kotest.multiplatform") version "6.0.0.M2"
    id("io.gitlab.arturbosch.detekt").version("1.23.5")
}

group = "app.minion"
version = "0.42.1.1"

repositories {
    mavenCentral()
    maven { url = uri("https://maven.pkg.jetbrains.space/public/p/kotlinx-html/maven") }
}

dependencies {
    implementation(npm("obsidian", "1.8.7"))

    val kotlinVersion = "2.1.10"
    val kotlinxHtmlVersion = "0.11.0"
    val kvisionVersion = "8.2.0"
    val arrowVersion = "2.0.1"
    val serializationVersion = "1.8.0"
    val loggingVersion = "3.0.5"
    val datetimeVersion = "0.6.2"
    val coroutinesCoreVersion = "1.10.1"
    val kotestVersion = "5.9.1"
    val kotestAssertionsArrowVersion = "2.0.0"
    val yamlktVersion = "0.13.0"
    val parsusVersion = "0.6.1"
    val betterParseVersion = "0.4.4"

    implementation("org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesCoreVersion")
    implementation("io.arrow-kt:arrow-core:$arrowVersion")
    implementation("io.arrow-kt:arrow-fx-coroutines:$arrowVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("net.mamoe.yamlkt:yamlkt:$yamlktVersion")
    implementation("io.github.microutils:kotlin-logging:$loggingVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:$datetimeVersion")
    implementation(npm("@js-joda/timezone", "2.3.0"))
    implementation("com.github.h0tk3y.betterParse:better-parse:$betterParseVersion")

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

project.plugins.withType<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsPlugin> {
    project.the<org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsEnvSpec>().download = false
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
            }
        }
    }
}

val copyTest = tasks.register<Copy>("copyTestExternals") {
    println("Copying test externals.js.test")
    from("webpack.config.d/externals.js.test")
    into("webpack.config.d")
    rename { "externals.js" }
}

val copyBuild = tasks.register<Copy>("copyBuildExternals") {
    println("Copying build externals.js.build")
    from("webpack.config.d/externals.js.build")
    into("webpack.config.d")
    rename { "externals.js" }
}

tasks.named("test") {
    dependsOn(":copyTestExternals")
}
tasks.named("browserDevelopmentWebpack") {
    dependsOn(":copyBuildExternals")
}
tasks.named("browserProductionWebpack") {
    dependsOn(":copyBuildExternals")
}

// OptIn to JsExport annotation
tasks.withType<org.jetbrains.kotlin.gradle.tasks.Kotlin2JsCompile>().configureEach {
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.RequiresOptIn"
    kotlinOptions.freeCompilerArgs += "-opt-in=kotlin.uuid.ExperimentalUuidApi"
}

detekt {
    // Version of detekt that will be used. When unspecified the latest detekt
    // version found will be used. Override to stay on the same version.
    toolVersion = "1.23.5"

    // The directories where detekt looks for source files.
    // Defaults to `files("src/main/java", "src/test/java", "src/main/kotlin", "src/test/kotlin")`.
    source.setFrom("src/main/kotlin", "src/test/kotlin")

    // Builds the AST in parallel. Rules are always executed in parallel.
    // Can lead to speedups in larger projects. `false` by default.
    parallel = false

    // Define the detekt configuration(s) you want to use.
    // Defaults to the default detekt configuration.
    //config.setFrom("path/to/config.yml")

    // Applies the config files on top of detekt's default config file. `false` by default.
    //buildUponDefaultConfig = false

    // Turns on all the rules. `false` by default.
    //allRules = false

    // Specifying a baseline file. All findings stored in this file in subsequent runs of detekt.
    //baseline = file("path/to/baseline.xml")

    // Disables all default detekt rulesets and will only run detekt with custom rules
    // defined in plugins passed in with `detektPlugins` configuration. `false` by default.
    //disableDefaultRuleSets = false

    // Adds debug output during task execution. `false` by default.
    debug = false

    // If set to `true` the build does not fail when the
    // maxIssues count was reached. Defaults to `false`.
    ignoreFailures = true

    // Specify the base path for file paths in the formatted reports.
    // If not set, all file paths reported will be absolute file path.
    basePath = projectDir.absolutePath
}
