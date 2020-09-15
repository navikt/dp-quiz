import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version Kotlin.version
    id(Spotless.spotless) version Spotless.version
}

repositories {
    jcenter()
}

allprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = Spotless.spotless)

    dependencies {
        implementation(kotlin("stdlib-jdk8"))

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

    tasks {
        /*tasks.named("compileKotlin") {
         dependsOn("spotlessCheck")
         } */
        withType<KotlinCompile>().all {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
        }

        named<KotlinCompile>("compileTestKotlin") {
            kotlinOptions.jvmTarget = JavaVersion.VERSION_1_8.toString()
        }

        withType<Test> {
            useJUnitPlatform()
            testLogging {
                showExceptions = true
                showStackTraces = true
                exceptionFormat = TestExceptionFormat.FULL
                events = setOf(TestLogEvent.PASSED, TestLogEvent.SKIPPED, TestLogEvent.FAILED)
            }
        }
    }
}

subprojects {
    apply(plugin = "org.jetbrains.kotlin.jvm")

    repositories {
        jcenter()
    }

    dependencies {
    }
}
