plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    `maven-publish`
}

group = "dev.klerkframework"
version = "0.1.0-SNAPSHOT"

dependencies {
    // Klerk Framework dependencies
    implementation("com.github.klerk-framework:klerk:${property("klerk_version")}")

    // Kotlin standard libraries
    implementation("org.jetbrains.kotlin:kotlin-stdlib")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:${property("coroutines_version")}")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:${property("serialization_version")}")

    // Logging
    implementation("org.slf4j:slf4j-api:${property("slf4j_version")}")
    implementation("ch.qos.logback:logback-classic:${property("logback_version")}")

    // Testing
    testImplementation(kotlin("test"))
}

tasks.test {
    useJUnitPlatform()
}
kotlin {
    jvmToolchain(17)
}
