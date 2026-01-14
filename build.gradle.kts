import com.diffplug.spotless.LineEnding
import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version "2.2.21"
    id("com.diffplug.spotless") version "8.1.0"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven {
        url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    }
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "com.diffplug.spotless")

    dependencies {
        implementation(kotlin("reflect"))

        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.2")
        testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.2")
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(21))
        }
    }

    spotless {
        kotlin {
            ktlint()
        }
        kotlinGradle {
            target("*.gradle.kts", "buildSrc/**/*.kt*")
            ktlint()
        }
        // Workaround for <https://github.com/diffplug/spotless/issues/1644>
        // using idea found at
        // <https://github.com/diffplug/spotless/issues/1527#issuecomment-1409142798>.
        lineEndings = LineEnding.PLATFORM_NATIVE // or any other except GIT_ATTRIBUTES
    }

    tasks.withType<Test> {
        useJUnitPlatform()
        testLogging {
            showExceptions = true
            showStackTraces = true
            exceptionFormat = TestExceptionFormat.FULL
            showStandardStreams = true
            events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
        }
        reports {
            junitXml.isOutputPerTestCase = true
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    repositories {
        mavenCentral()
        maven {
            url = uri("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        }
    }

    tasks.named("compileKotlin") {
        dependsOn("spotlessApply")
    }

    dependencies {
        testImplementation(kotlin("test"))
        testImplementation("org.junit.jupiter:junit-jupiter-api:6.0.2")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:6.0.2")
    }
}
