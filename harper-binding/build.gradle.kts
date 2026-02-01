plugins {
    kotlin("jvm")
    kotlin("plugin.serialization") version "2.3.0"
}

dependencies {
    implementation(libs.kotlinx.serialization.json)
}

val rustBasePath = layout.projectDirectory.dir("src/main/rust")
val jniLibsDir = layout.buildDirectory.dir("jniLibs")

val androidTargets = listOf("aarch64-linux-android", "x86_64-linux-android","armv7-linux-androideabi")
val desktopTarget = "x86_64-unknown-linux-gnu"

tasks.register("cargoBuildAndroid") {
    group = "rust"
    description = "Builds the Rust library for Android targets"

    doFirst {
        file(jniLibsDir).mkdirs()
    }

    doLast {
        androidTargets.forEach { target ->
            exec {
                workingDir = layout.projectDirectory.asFile
                commandLine("cargo", "ndk", "-t", target, "-o", jniLibsDir.get().asFile.absolutePath, "build", "--release")
            }
        }
    }
}

tasks.register("cargoBuildDesktop") {
    group = "rust"
    description = "Builds the Rust library for Desktop (Linux)"

    doLast {
        exec {
            workingDir = layout.projectDirectory.asFile
            commandLine("cargo", "build", "--release", "--target", desktopTarget)
        }

        copy {
            from("$rustBasePath/target/$desktopTarget/release/liblimae_harper.so")
            into(layout.buildDirectory.dir("resources/main"))
        }
    }
}

tasks.named("processResources") {
    dependsOn("cargoBuildDesktop")
}

tasks.named("compileKotlin") {
    dependsOn("cargoBuildDesktop")
}

configurations.create("jniLibs")
artifacts.add("jniLibs", jniLibsDir.get().asFile) {
    builtBy("cargoBuildAndroid")
}