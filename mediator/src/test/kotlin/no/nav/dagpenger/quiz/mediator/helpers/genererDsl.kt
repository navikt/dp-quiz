package no.nav.dagpenger.quiz.mediator.helpers

import java.io.File

private val homeDirectory = System.getProperty("user.home")
private val quizshowPath = "$homeDirectory/pathTilQuizshow"
private val faktaPath = "$quizshowPath/src/soknad-fakta"

fun main() {
    val komplettFakta = File(faktaPath)
    if (verifiserAtFilEksisterer(komplettFakta)) return
    val faktaFiles: Array<File>? = komplettFakta.listFiles { file -> !file.name.contains("soknad.ts") }

    faktaFiles?.forEach { file ->
        println("Fil: $file")
        val fileAsString = file.readText(Charsets.UTF_8)
        val fileAsJson = fileAsString.fjernTypescriptSyntax()
        val quizDsl = BffTilDslGenerator(fileAsJson)
        println("$quizDsl\n")
    }
}

private fun verifiserAtFilEksisterer(komplettFakta: File): Boolean {
    if (!komplettFakta.exists()) {
        println("Fant ikke mappa, lette etter $komplettFakta")
        return true
    }
    return false
}
