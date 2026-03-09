import org.khorum.oss.plugins.open.publishing.digitalocean.domain.uploadToDigitalOceanSpaces
import org.khorum.oss.plugins.open.publishing.mavengenerated.domain.mavenGeneratedArtifacts
import org.khorum.oss.plugins.open.secrets.getPropertyOrEnv
import kotlin.apply

plugins {
	kotlin("jvm") version "2.3.0"
	id("dev.detekt") version "2.0.0-alpha.2"
	id("org.jetbrains.dokka") version "2.1.0"
	id("org.jetbrains.dokka-javadoc") version "2.1.0"
	id("org.jetbrains.kotlinx.kover") version "0.9.4"
	id("org.sonarqube") version "7.0.0.6105"
	id("org.khorum.oss.plugins.open.publishing.maven-generated-artifacts") version "1.0.3"
	id("org.khorum.oss.plugins.open.publishing.digital-ocean-spaces") version "1.0.3"
	id("org.khorum.oss.plugins.open.secrets") version "1.0.0"
	id("org.khorum.oss.plugins.open.pipeline") version "1.0.0"
}

group = "org.khorum.oss.spektr"
version = file("VERSION").readText().trim()

// Bridge Dokka v1 task names to v2 for maven-generated-artifacts plugin compatibility
tasks.register("dokkaJavadoc") {
	group = "documentation"
	description = "Generate Javadoc API documentation"
	dependsOn("dokkaGeneratePublicationJavadoc")
}
tasks.register("dokkaHtml") {
	group = "documentation"
	description = "Generate HTML API documentation"
	dependsOn("dokkaGeneratePublicationHtml")
}

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

sonar {
	properties {
		property("sonar.projectKey", "khorum-oss_spektr-dsl")
		property("sonar.organization", "khorum-oss")
		property("sonar.host.url", "https://sonarcloud.io")
		property("sonar.coverage.jacoco.xmlReportPaths",
			"${layout.buildDirectory.get()}/reports/kover/report.xml")
	}
}
