plugins {
    kotlin("jvm") version "2.1.10"
    id("org.jetbrains.kotlinx.benchmark") version "0.4.13"
    kotlin("plugin.allopen") version "2.0.20"
}
allOpen {
    annotation("org.openjdk.jmh.annotations.State")
}

group = "io.github.hangga"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    gradlePluginPortal()
}

dependencies {
    testImplementation(kotlin("test"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.1")
    implementation("org.jetbrains.kotlinx:kotlinx-benchmark-runtime:0.4.13")
}

benchmark {
    targets {
        register("main")
    }
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(11)
}