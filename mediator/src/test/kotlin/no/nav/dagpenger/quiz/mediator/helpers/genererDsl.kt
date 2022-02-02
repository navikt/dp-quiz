package no.nav.dagpenger.quiz.mediator.helpers

import java.io.File

private val homeDirectory = System.getProperty("user.home")
private val quizshowPath = "$homeDirectory/dev/code/dagpenger/dp-quizshow"
private val faktaPath = "$quizshowPath/src/soknad-fakta"
private val faktaFiles: Array<File>? = File(faktaPath).listFiles { file -> !file.name.contains("soknad.ts") }

fun main() {
    faktaFiles?.forEach { file ->
        println("Fil: $file")
        val fileAsString = file.readText(Charsets.UTF_8)
        val fileAsJson = fileAsString.fjernTypescriptSyntax()
        val quizDsl = BffTilDslGenerator(fileAsJson)
        println("$quizDsl\n")
    }
}
