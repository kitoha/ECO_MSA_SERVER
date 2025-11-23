import org.gradle.testing.jacoco.tasks.JacocoReport
import org.gradle.api.tasks.testing.Test
import org.gradle.testing.jacoco.plugins.JacocoTaskExtension

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

java {
  toolchain {
    languageVersion.set(JavaLanguageVersion.of(21))
  }
}

dependencies {
  implementation(libs.spring.boot.starter.web)
  implementation(libs.jackson.module.kotlin)
  implementation(libs.spring.boot.starter.data.jpa)
  implementation(libs.kotlin.reflect)
  implementation(libs.postgresql)
  implementation("com.querydsl:querydsl-core:${libs.versions.querydsl.get()}")
  implementation("com.querydsl:querydsl-jpa:${libs.versions.querydsl.get()}:jakarta")
  kapt("com.querydsl:querydsl-apt:${libs.versions.querydsl.get()}:jakarta")
  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.kotlin.test.junit5)
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.kotlin.test)
  implementation(libs.spring.boot.starter.validation)

  // Kotest
  testImplementation(libs.kotest.runner.junit5)
  testImplementation(libs.kotest.assertions.core)
  testImplementation(libs.kotest.property)
  testImplementation(libs.kotest.extensions.spring)

  // Mockk
  testImplementation(libs.mockk)

  // TestContainers
  testImplementation(platform(libs.testcontainers.bom))
  testImplementation(libs.testcontainers.postgresql)
  testImplementation(libs.testcontainers.junit.jupiter)
  testImplementation("org.springframework.boot:spring-boot-testcontainers")
}

kotlin {
  jvmToolchain(21)
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

    extensions.configure(JacocoTaskExtension::class) {
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