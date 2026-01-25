plugins {
	alias(libs.plugins.kotlin.jvm) apply false
	alias(libs.plugins.kotlin.spring) apply false
	alias(libs.plugins.kotlin.jpa) apply false
	alias(libs.plugins.kotlin.kapt) apply false
	alias(libs.plugins.spring.boot) apply false
	alias(libs.plugins.spring.dependency.management) apply false
	alias(libs.plugins.sonarqube)
	alias(libs.plugins.jib) apply false
	jacoco
}

group = "com.eco"
version = "0.0.1-SNAPSHOT"

repositories {
	mavenCentral()
}

subprojects {
	apply(plugin = "jacoco")
	apply(plugin = "com.google.cloud.tools.jib")

	configure<com.google.cloud.tools.jib.gradle.JibExtension> {
		from {
			image = "eclipse-temurin:21-jre-alpine"
			platforms {
				platform {
					architecture = "arm64"
					os = "linux"
				}
			}
		}
		to {
			image = "eco-msa/${project.name}:latest"
		}
		container {
			creationTime = "USE_CURRENT_TIMESTAMP"
			jvmFlags = listOf("-Dspring.profiles.active=docker", "-Xms512m", "-Xmx512m")
		}
	}

	afterEvaluate {
		if (buildFile.exists()) {
			tasks.withType<Test> {
				useJUnitPlatform()
			}

			tasks.matching { it.name == "jacocoTestReport" }.configureEach {
				if (this is JacocoReport) {
					dependsOn(tasks.withType<Test>())
					reports {
						xml.required.set(true)
						html.required.set(true)
					}

					val integrationTestTask = tasks.findByName("integrationTest")
					if (integrationTestTask != null) {
						executionData.setFrom(
							fileTree(layout.buildDirectory) {
								include("jacoco/test.exec", "jacoco/integrationTest.exec")
							}
						)
					}

					classDirectories.setFrom(
						files(classDirectories.files.map {
							fileTree(it) {
								exclude(
									"**/config/**",
									"**/dto/**",
									"**/entity/**",
									"**/*Application*.class"
								)
							}
						})
					)
				}
			}
		}
	}
}

sonarqube {
	properties {
		property("sonar.projectKey", "kitoha_ECO_MSA_SERVER")
		property("sonar.organization", "kitoha")
		property("sonar.host.url", "https://sonarcloud.io")
		property(
			"sonar.coverage.jacoco.xmlReportPaths",
			"**/build/reports/jacoco/test/jacocoTestReport.xml"
		)

		property(
			"sonar.coverage.exclusions",
			"**/config/**," +
					"**/dto/**," +
					"**/entity/**,"
		)
	}
}
