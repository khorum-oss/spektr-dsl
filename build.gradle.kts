import kotlinx.kover.gradle.plugin.dsl.KoverProjectExtension
import org.khorum.oss.plugins.open.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import org.khorum.oss.plugins.open.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import org.khorum.oss.plugins.open.secrets.getPropertyOrEnv
import kotlin.apply

plugins {
	kotlin("jvm") version "2.3.0"
	kotlin("plugin.spring") version "2.3.0"
	id("org.springframework.boot") version "4.1.0-M1"
	id("io.spring.dependency-management") version "1.1.7"
	id("dev.detekt") version "2.0.0-alpha.2"
	id("org.jetbrains.dokka") version "2.1.0"
	id("org.jetbrains.dokka-javadoc") version "2.1.0"
	id("org.jetbrains.kotlinx.kover") version "0.7.6"
	id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts") version "1.0.0"
	id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces") version "1.0.0"
	id("org.khorum.oss.plugins.open.secrets") version "1.0.0"
	id("org.khorum.oss.plugins.open.pipeline") version "1.0.0"
}

group = "org.khorum.oss.spektr"
version = file("VERSION").readText().trim()

// Root project is not a Spring Boot application
tasks.bootJar { enabled = false }
tasks.jar { enabled = false }

// Bridge Dokka v1 task names to v2 for maven-generated-artifacts plugin compatibility
tasks.register("dokkaJavadoc") {
	dependsOn("dokkaGeneratePublicationJavadoc")
}
tasks.register("dokkaHtml") {
	dependsOn("dokkaGeneratePublicationHtml")
}

repositories {
	mavenCentral()
}

dependencies {
	implementation("io.github.microutils:kotlin-logging:4.0.0-beta-2")

	testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
	testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

// Disable Kover instrumentation globally to avoid race condition
// with kover-agent.args file during parallel builds (Kover 0.7.x bug)
extensions.configure<KoverProjectExtension> {
	disable()
}

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(21)
	}
}

digitalOceanSpacesPublishing {
	bucket = "open-reliquary"
	accessKey = project.getPropertyOrEnv("spaces.key", "DO_SPACES_API_KEY")
	secretKey = project.getPropertyOrEnv("spaces.secret", "DO_SPACES_SECRET")
	publishedVersion = version.toString()
	isPlugin = false
	dryRun = false
}

tasks.uploadToDigitalOceanSpaces?.apply {
	val task: Task = tasks.mavenGeneratedArtifacts ?: throw Exception("mavenGeneratedArtifacts task not found")
	dependsOn(task)
}

mavenGeneratedArtifacts {
	publicationName = "digitalOceanSpaces"  // Must match the name expected by the DO Spaces plugin
	name = "Spektr DSL"
	description = """
            A DSL library for creating Spektr APIs.
        """
	websiteUrl = "https://github.com/khorum-oss/spektr-dsl/tree/main/src"

	licenses {
		license {
			name = "MIT License"
			url = "https://opensource.org/license/mit"
		}
	}

	developers {
		developer {
			id = "khorum-oss"
			name = "Khorum OSS Team"
			email = "khorum.oss@gmail.com"
			organization = "Khorum OSS"
		}
	}

	scm {
		connection.set("https://github.com/khorum-oss/spektr-dsl.git")
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
