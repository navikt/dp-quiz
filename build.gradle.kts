import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
}

repositories {
    mavenCentral()
    maven("https://packages.confluent.io/maven/")
    maven("https://jitpack.io")
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = Spotless.spotless)
    tasks.withType<KotlinCompile>().all {
        kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
    }

    dependencies {
        implementation(kotlin("stdlib"))
        implementation(kotlin("reflect"))

        testRuntimeOnly(Junit5.engine)
        testImplementation(Junit5.api)
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
        implementation(kotlin("stdlib-jdk8"))
        testImplementation(kotlin("test"))
        testImplementation(Junit5.api)
        testRuntimeOnly(Junit5.engine)
    }
}
