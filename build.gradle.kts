import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.9.22"
    kotlin("plugin.serialization") version "1.9.22"
    `maven-publish`
}

group = "com.github.klerk-framework"
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
    testImplementation("org.jetbrains.kotlin:kotlin-test")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.junit.jupiter:junit-jupiter-api:${property("junit_version")}")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:${property("junit_version")}")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += "-Xjsr305=strict"
    }
}

java {
    withSourcesJar()
    withJavadocJar()
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

publishing {
    publications {
        create<MavenPublication>("maven") {
            from(components["java"])

            pom {
                name.set("klerk-mcp")
                description.set("A Klerk plugin to expose a Klerk-based application as a Model Context Protocol (MCP) API")
                url.set("https://github.com/klerk-framework/klerk-mcp")

                licenses {
                    license {
                        name.set("The Apache License, Version 2.0")
                        url.set("http://www.apache.org/licenses/LICENSE-2.0.txt")
                    }
                }

                developers {
                    developer {
                        id.set("klerk-framework")
                        name.set("Klerk Framework Team")
                        email.set("info@klerk-framework.com")
                    }
                }

                scm {
                    connection.set("scm:git:git://github.com/klerk-framework/klerk-mcp.git")
                    developerConnection.set("scm:git:ssh://github.com/klerk-framework/klerk-mcp.git")
                    url.set("https://github.com/klerk-framework/klerk-mcp")
                }
            }
        }
    }
}
