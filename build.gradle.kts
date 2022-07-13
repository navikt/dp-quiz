import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile


plugins {
    application
    kotlin("jvm") version Kotlin.version
    id("org.jlleitschuh.gradle.ktlint") version "10.3.0"
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    tasks.withType<KotlinCompile>().all {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        testRuntimeOnly(Junit5.engine)
        testImplementation(Junit5.api)
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

    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    tasks.withType<KotlinCompile>().configureEach {
        dependsOn("ktlintFormat")
    }

    ktlint {
        version.set(Ktlint.version)
    }

    dependencies {
        implementation(kotlin("stdlib-jdk8"))
        testImplementation(kotlin("test"))
        testImplementation(Junit5.api)
        testRuntimeOnly(Junit5.engine)
    }
}
