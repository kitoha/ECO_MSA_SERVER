import org.gradle.api.tasks.testing.Test

plugins {
    alias(libs.plugins.spring.boot)
    alias(libs.plugins.spring.dependency.management)
    alias(libs.plugins.kotlin.jvm)
    alias(libs.plugins.kotlin.spring)
    alias(libs.plugins.kotlin.jpa)
    alias(libs.plugins.kotlin.kapt)
}

group = "com.eco"
version = "0.0.1-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    // Web & Security
    implementation(libs.spring.boot.starter.web)
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-client")

    // Kotlin
    implementation(libs.jackson.module.kotlin)
    implementation(libs.kotlin.reflect)

    // Data (JPA, Postgres, Flyway)
    implementation(libs.spring.boot.starter.data.jpa)
    implementation(libs.postgresql)
    implementation(libs.flyway.core)
    implementation(libs.flyway.database.postgresql)

    // JWT (Gateway와 동일)
    implementation(libs.jjwt.api)
    runtimeOnly(libs.jjwt.impl)
    runtimeOnly(libs.jjwt.jackson)

    // Validation
    implementation("org.springframework.boot:spring-boot-starter-validation")

    // Eureka Client
    implementation(libs.spring.cloud.starter.netflix.eureka.client)

    // Test
    testImplementation(libs.spring.boot.starter.test)
    testImplementation("org.springframework.security:spring-security-test")
    testImplementation(libs.kotlin.test.junit5)
    testRuntimeOnly(libs.junit.platform.launcher)

    // Kotest & Mockk
    testImplementation(libs.kotest.runner.junit5)
    testImplementation(libs.kotest.assertions.core)
    testImplementation(libs.kotest.property)
    testImplementation(libs.kotest.extensions.spring)
    testImplementation(libs.mockk)

    // Testcontainers
    testImplementation(platform(libs.testcontainers.bom))
    testImplementation(libs.testcontainers.postgresql)
    testImplementation(libs.testcontainers.junit.jupiter)
    testImplementation("org.springframework.boot:spring-boot-testcontainers")

    // Monitoring
    implementation(libs.spring.boot.starter.actuator)
    implementation(libs.micrometer.registry.prometheus)
}

dependencyManagement {
    imports {
        mavenBom(libs.spring.cloud.dependencies.get().toString())
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict")
    }
}

sourceSets {
    create("integrationTest") {
        kotlin {
            srcDir("src/integrationTest/kotlin")
            compileClasspath += sourceSets["main"].output + sourceSets["test"].output
            runtimeClasspath += sourceSets["main"].output + sourceSets["test"].output
        }
        resources {
            srcDir("src/integrationTest/resources")
        }
    }
}

configurations {
    getByName("integrationTestImplementation") {
        extendsFrom(configurations["testImplementation"])
    }
    getByName("integrationTestRuntimeOnly") {
        extendsFrom(configurations["testRuntimeOnly"])
    }
}

tasks {
    register<Test>("integrationTest") {
        description = "Runs integration tests."
        group = "verification"

        testClassesDirs = sourceSets["integrationTest"].output.classesDirs
        classpath = sourceSets["integrationTest"].runtimeClasspath

        shouldRunAfter("test")

        useJUnitPlatform()

        extensions.configure(org.gradle.testing.jacoco.plugins.JacocoTaskExtension::class) {
            setDestinationFile(layout.buildDirectory.file("jacoco/integrationTest.exec").get().asFile)
        }
    }

    named("check") {
        dependsOn("integrationTest")
    }

    named<ProcessResources>("processIntegrationTestResources") {
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }
}

tasks.withType<Test> {
    useJUnitPlatform()
}
