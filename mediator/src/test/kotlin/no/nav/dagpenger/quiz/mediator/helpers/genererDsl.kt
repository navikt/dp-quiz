package no.nav.dagpenger.quiz.mediator.helpers

import java.io.File

private val homeDirectory = System.getProperty("user.home")
private val soknadsdialogPath = "$homeDirectory/pathTo/dp-soknadsdialog"
private val faktaPath = "$soknadsdialogPath/src/soknad-fakta"

fun main() {
    val faktaseksjoner = lesInnFaktaseksjoner()

    faktaseksjoner?.forEachIndexed { index, seksjonsfil ->
        println("Fil: $seksjonsfil")
        val fileAsString = seksjonsfil.readText(Charsets.UTF_8)
        val fileAsJson = fileAsString.fjernTypescriptSyntax()
        val startpunktForDatabaseIdTeller = (index * 1000) + 1
        val quizDsl = BffTilDslGenerator(fileAsJson, startpunktForDatabaseIdTeller)
        println("$quizDsl\n")
    }
}

private fun lesInnFaktaseksjoner(): Array<File>? {
    val mappeMedFrontendFaktaseksjoner = File(faktaPath)
    verifiserAtMappaEksisterer(mappeMedFrontendFaktaseksjoner)
    val faktaseksjoner: Array<File>? = mappeMedFrontendFaktaseksjoner.listFiles { seksjonsfil ->
        filterBortUnødvendigeSeksjoner(seksjonsfil)
    }
    return faktaseksjoner
}

private fun verifiserAtMappaEksisterer(path: File) {
    if (!path.exists()) {
        throw IllegalArgumentException("Fant ikke mappa: '$path'. Vennligst spesifiser lokal path til 'dp-soknad'.")
    }
}

private fun filterBortUnødvendigeSeksjoner(file: File) =
    !(file.name.contains("soknad.ts") || file.name.contains("dummy-seksjon.ts"))
