plugins {
  alias(libs.plugins.spring.boot)
  alias(libs.plugins.spring.dependency.management)
  alias(libs.plugins.kotlin.jvm)
  alias(libs.plugins.kotlin.spring)
  alias(libs.plugins.kotlin.jpa)
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
  testImplementation(libs.spring.boot.starter.test)
  testImplementation(libs.kotlin.test.junit5)
  testRuntimeOnly(libs.junit.platform.launcher)
  testImplementation(libs.kotlin.test)
}

kotlin {
  jvmToolchain(21)
}