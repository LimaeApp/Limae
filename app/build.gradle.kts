import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    id("app.cash.sqldelight") version "2.2.1"
    kotlin("plugin.serialization") version "2.0.20"
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_11)
        }
    }

    jvm()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        binaries.executable()
    }
    sourceSets.all {
        languageSettings.enableLanguageFeature("ExplicitBackingFields")
    }
    sourceSets {
        androidMain.dependencies {
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.activity.compose)
            implementation("app.cash.sqldelight:android-driver:2.2.1")

            implementation(libs.koin.android)
            implementation(libs.koin.androidx.compose)
            implementation(libs.androidx.documentfile)

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
        }

        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            implementation(project(":harper-binding"))
            implementation(libs.navigation.compose)
            implementation(libs.compose.components.resources)
            implementation(libs.kotlinx.collections.immutable)

            implementation("app.cash.sqldelight:runtime:2.2.1")
            implementation("app.cash.sqldelight:coroutines-extensions:2.2.1")
            implementation(libs.kotlinx.datetime)
            implementation(libs.kotlinx.serialization.json)

            api(libs.koin.core)
            implementation(libs.koin.compose)
            implementation(libs.koin.compose.viewmodel)
        }

        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }

        jvmMain.dependencies {
            implementation(compose.desktop.currentOs)
            implementation(libs.kotlinx.coroutinesSwing)
            implementation("org.languagetool:languagetool-core:6.7")
            implementation("org.languagetool:language-en:6.7")
            implementation("app.cash.sqldelight:sqlite-driver:2.2.1")

            implementation(libs.androidx.datastore)
            implementation(libs.androidx.datastore.preferences)
        }
        webMain.dependencies {
            implementation(libs.kotlinx.serialization.json)
            implementation("app.cash.sqldelight:web-worker-driver:2.2.1")
            implementation("org.jetbrains.kotlinx:kotlinx-browser:0.5.0")
        }
    }
}

android {
    namespace = "com.sakethh.limae"
    compileSdk =
        libs.versions.android.compileSdk
            .get()
            .toInt()

    defaultConfig {
        applicationId = "com.sakethh.limae"
        minSdk =
            libs.versions.android.minSdk
                .get()
                .toInt()
        targetSdk =
            libs.versions.android.targetSdk
                .get()
                .toInt()
        versionCode = 1
        versionName = "1.0"
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
        }

        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    sourceSets {
        getByName("main") {
            jniLibs.srcDir(rootProject.file("harper-binding/build/jniLibs"))
        }
    }
}

dependencies {
    debugImplementation(libs.compose.uiTooling)
}

compose.desktop {
    application {
        mainClass = "com.sakethh.limae.MainKt"

        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "com.sakethh.limae"
            packageVersion = "1.0.0"
        }

        val rustTarget = "x86_64-unknown-linux-gnu"
        val rustBuildDir =
            project(":harper-binding").projectDir.resolve("target/$rustTarget/release")

        jvmArgs += "-Djava.library.path=${rustBuildDir.absolutePath}"
    }
}

tasks.withType<JavaExec>().configureEach {
    if (name == "hotRunJvm") {
        val rustTarget = "x86_64-unknown-linux-gnu"
        val rustBuildDir =
            project(":harper-binding").projectDir.resolve("target/$rustTarget/release")
        systemProperty("java.library.path", rustBuildDir.absolutePath)
    }
}

sqldelight {
    databases.create("LimaeDatabase") {
        packageName.set("com.sakethh.limae")
        generateAsync.set(true)
    }
}
