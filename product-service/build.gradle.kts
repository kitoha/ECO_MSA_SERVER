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
}

kotlin {
  jvmToolchain(21)
}