import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

plugins {
    application
    kotlin("jvm") version "1.8.0"
    id(Spotless.spotless) version "6.11.0"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = Spotless.spotless)

    dependencies {
        implementation(kotlin("reflect"))

        testRuntimeOnly(Junit5.engine)
        testImplementation(Junit5.api)
    }

    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }

    spotless {
        kotlin {
            ktlint(Ktlint.version)
        }
        kotlinGradle {
            target("*.gradle.kts", "buildSrc/**/*.kt*")
            ktlint(Ktlint.version)
        }
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
        maven("https://jitpack.io")
    }

    tasks.named("compileKotlin") {
        dependsOn("spotlessApply")
    }

    dependencies {
        testImplementation(kotlin("test"))
        testImplementation(Junit5.api)
        testRuntimeOnly(Junit5.engine)
    }
}
