plugins {
    kotlin("multiplatform")
    id("com.android.library")
    kotlin("plugin.serialization") version "2.3.0"
}

kotlin {
    androidTarget {
        publishLibraryVariants("release", "debug")
    }

    jvm()

    wasmJs {
        browser {
            commonWebpackConfig {
                outputFileName = "harper-binding.js"
            }
        }
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation(libs.kotlinx.serialization.json)
                compileOnly("androidx.compose.runtime:runtime-annotation:1.9.0")
            }
        }

        val jvmAndAndroidMain by creating {
            dependsOn(commonMain)
        }

        val jvmAndAndroidMainTest by creating {
            val commonTest by getting
            dependsOn(commonTest)

            dependencies {
                implementation(libs.kotlin.test)
            }
        }

        val androidMain by getting {
            dependsOn(jvmAndAndroidMain)
        }
        val jvmMain by getting {
            dependsOn(jvmAndAndroidMain)
        }
        val androidInstrumentedTest by getting {
            dependsOn(jvmAndAndroidMainTest)
            androidInstrumentedTest.dependencies {
                implementation("androidx.test.ext:junit:1.2.1")
                implementation("androidx.test:runner:1.6.1")
                implementation("androidx.test:rules:1.6.1")
            }
        }
        val jvmTest by getting {
            dependsOn(jvmAndAndroidMainTest)
        }

        val wasmJsMain by getting
    }
}

android {
    namespace = "com.sakethh.limae.harperbinding"
    compileSdk = 36
    defaultConfig {
        minSdk = 24
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    sourceSets {
        getByName("main") {
            jniLibs.srcDir(rootProject.file("harper-binding/build/jniLibs"))
        }
    }
}


val rustBasePath = layout.projectDirectory.asFile
val jniLibsDir = layout.buildDirectory.dir("jniLibs")

tasks.register("cargoBuildAndroid") {
    group = "rust"
    doLast {
        val ndkBase = file("${System.getProperty("user.home")}/Android/Sdk/ndk")
        val ndkDir = ndkBase.listFiles()?.maxOrNull()
            ?: throw GradleException("NDK not found in $ndkBase. Please install it via Android Studio SDK Manager.")

        listOf(
            "aarch64-linux-android",
            "x86_64-linux-android",
            "armv7-linux-androideabi"
        ).forEach { target ->
            exec {
                workingDir = rustBasePath
                environment("ANDROID_NDK_HOME", ndkDir.absolutePath)
                commandLine(
                    "cargo",
                    "ndk",
                    "-t",
                    target,
                    "-o",
                    jniLibsDir.get().asFile.absolutePath,
                    "build",
                    "--release"
                )
            }
        }
    }
}

tasks.register("cargoBuildDesktop") {
    group = "rust"
    doLast {
        val desktopTarget = "x86_64-unknown-linux-gnu"

        exec {
            workingDir = rustBasePath
            commandLine("cargo", "build", "--release", "--target", desktopTarget)
        }

        val libDir = layout.buildDirectory.dir("rustLibs/desktop").get().asFile
        libDir.mkdirs()

        val sourceFile = File("$rustBasePath/target/$desktopTarget/release/libharper_binding.so")

        if (sourceFile.exists()) {
            println("Copying native lib from: ${sourceFile.absolutePath}")
            println("To: ${libDir.absolutePath}")
            copy {
                from(sourceFile)
                into(libDir)
            }
        } else {
            throw GradleException("Rust build failed or output file not found at: ${sourceFile.absolutePath}")
        }
    }
}

tasks.register("cargoBuildWasm") {
    group = "rust"
    doLast {
        exec {
            workingDir = rustBasePath
            commandLine("wasm-pack", "build", "--target", "bundler", "--release")
        }
    }
}

tasks.matching { it.name == "preBuild" }.configureEach {
    dependsOn("cargoBuildAndroid")
}

tasks.matching { it.name == "processJvmResources" || it.name == "jvmProcessResources" }.configureEach {
    dependsOn("cargoBuildDesktop")
}

tasks.matching { it.name == "compileKotlinWasmJs" }.configureEach {
    dependsOn("cargoBuildWasm")
}

tasks.named<Test>("jvmTest") {
    dependsOn("cargoBuildDesktop")

    val libPath = layout.buildDirectory.dir("rustLibs/desktop").get().asFile.absolutePath

    doFirst {
        println("jvmTest using: $libPath")
        val libFile = file("$libPath/libharper_binding.so")
        if (libFile.exists()) {
            println("Found library file: ${libFile.name}")
        } else {
            println("LIBRARY FILE MISSING in $libPath")
            file(libPath).listFiles()?.forEach { println("   - Found: ${it.name}") }
        }
    }

    systemProperty("java.library.path", libPath)

    testLogging {
        events("passed", "skipped", "failed")
        showStandardStreams = true
    }
}