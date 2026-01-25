plugins {
    alias(libs.plugins.kotlin.jvm)
    id("com.google.protobuf") version "0.9.4"
    `maven-publish`
}

group = "com.eco"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Protobuf
    api("com.google.protobuf:protobuf-kotlin:3.25.1")
    api("com.google.protobuf:protobuf-java-util:3.25.1")

    // Kotlin
    implementation(libs.kotlin.reflect)

    // Testing
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotlin.test.junit5)
}

protobuf {
    protoc {
        artifact = "com.google.protobuf:protoc:3.25.1"
    }

    generateProtoTasks {
        all().forEach { task ->
            task.builtins {
                create("kotlin")
            }
        }
    }
}

sourceSets {
    main {
        proto {
            srcDir("src/main/proto")
        }
    }
}

tasks.withType<ProcessResources> {
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

tasks.withType<Test> {
    useJUnitPlatform()
}
