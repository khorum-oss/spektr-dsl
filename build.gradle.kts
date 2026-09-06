plugins {
	kotlin("jvm") version "2.3.0"
	id("dev.detekt") version "2.0.0-alpha.2"
	id("org.jetbrains.dokka") version "2.1.0"
	id("org.jetbrains.dokka-javadoc") version "2.1.0"
	id("org.jetbrains.kotlinx.kover") version "0.9.4"
	`maven-publish`
	signing
}

group = "org.khorum.oss.spektr"
version = file("VERSION").readText().trim()

repositories {
	mavenCentral()
}

val loggingVersion = "4.0.0-beta-2"

dependencies {
	implementation("io.github.microutils:kotlin-logging:$loggingVersion")

	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
	withSourcesJar()
}

/**
 * Reads a private value from a Gradle property first, then falls back to an environment variable.
 * Nothing sensitive lives in this file: supply the values via `~/.gradle/gradle.properties`,
 * `-P` flags, or the environment (CI secrets).
 */
fun secret(propertyName: String, environmentName: String): String? =
	providers.gradleProperty(propertyName).orNull
		?: providers.environmentVariable(environmentName).orNull

val dokkaJavadocJar by tasks.registering(Jar::class) {
	group = "documentation"
	description = "Packages the Dokka Javadoc output as a publishable jar"
	archiveClassifier.set("javadoc")
	from(tasks.named("dokkaGeneratePublicationJavadoc"))
}

publishing {
	publications {
		create<MavenPublication>("maven") {
			from(components["java"])
			artifact(dokkaJavadocJar)

			pom {
				name.set("Spektr DSL")
				description.set("A DSL library for creating Spektr APIs.")
				url.set("https://github.com/khorum-oss/spektr-dsl/tree/main/src")

				licenses {
					license {
						name.set("MIT License")
						url.set("https://opensource.org/license/mit")
					}
				}

				developers {
					developer {
						id.set("khorum-oss")
						name.set("Khorum OSS Team")
						email.set("khorum.oss@gmail.com")
						organization.set("Khorum OSS")
					}
				}

				scm {
					connection.set("https://github.com/khorum-oss/spektr-dsl.git")
					url.set("https://github.com/khorum-oss/spektr-dsl")
				}
			}
		}
	}

	repositories {
		// Target repository comes from private configuration:
		//   publish.repo.url      / MAVEN_REPO_URL
		//   publish.repo.username / MAVEN_REPO_USERNAME
		//   publish.repo.password / MAVEN_REPO_PASSWORD
		// Without a URL only `publishToMavenLocal` is available.
		val repoUrl = secret("publish.repo.url", "MAVEN_REPO_URL")

		if (repoUrl != null) {
			val repoUser = secret("publish.repo.username", "MAVEN_REPO_USERNAME")
			val repoPassword = secret("publish.repo.password", "MAVEN_REPO_PASSWORD")

			maven {
				name = "private"
				url = uri(repoUrl)

				// Credentials stay unset for protocols that reject them, such as `file:`.
				if (repoUser != null || repoPassword != null) {
					credentials {
						username = repoUser
						password = repoPassword
					}
				}
			}
		}
	}
}

signing {
	// Artifacts are signed only when a key is supplied:
	//   signing.key      / GPG_SIGNING_KEY       (ASCII-armored private key)
	//   signing.password / GPG_SIGNING_PASSWORD
	val signingKey = secret("signing.key", "GPG_SIGNING_KEY")
	val signingPassword = secret("signing.password", "GPG_SIGNING_PASSWORD")

	setRequired { signingKey != null }

	if (signingKey != null) {
		useInMemoryPgpKeys(signingKey, signingPassword ?: "")
		sign(publishing.publications["maven"])
	}
}

tasks.test {
	useJUnitPlatform()
}

detekt {
	buildUponDefaultConfig = true
	allRules = false
	config.setFrom(files("$rootDir/detekt.yml"))
	source.setFrom("src/main/kotlin")
	parallel = true
}
