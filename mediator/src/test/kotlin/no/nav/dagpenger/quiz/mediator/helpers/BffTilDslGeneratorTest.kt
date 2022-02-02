package no.nav.dagpenger.quiz.mediator.helpers

import org.junit.jupiter.api.Test
import java.io.File

class BffTilDslGeneratorTest {

    private val homeDirectory = System.getProperty("user.home")
    private val quizshowPath = "$homeDirectory/dev/code/dagpenger/dp-quizshow"
    private val faktaPath = "$quizshowPath/src/soknad-fakta"

    @Test
    fun `Skal kunne konvertere fra BFF-json-seksjoner til Quiz-DSL`() {
        val faktaFiles = File(faktaPath).listFiles{ file -> !file.name.contains("soknad.ts") }

        faktaFiles?.forEach { file ->
            println("Fil: $file")
            val fileAsString = file.readText(Charsets.UTF_8)
            val fileAsJson = fileAsString.fjernTypescriptSyntax()
            val quizDsl = BffTilDslGenerator(fileAsJson)
            println("$quizDsl\n")
        }
    }

    private fun String.fjernTypescriptSyntax(): String =
        replace(Regex("import .*"), "")
            .replace(Regex("export .*"), "{")
}
