package no.nav.dagpenger.quiz.mediator.land

import no.nav.dagpenger.model.faktum.Land

internal object Landfabrikken {
    private val land = LandOppslag.land

    private const val landkodeForKosovo = "XXK"
    private const val pdlKodeForStatsløs = "XXX"
    private const val pdlKodeForUkjentLand = "XUK"
    private val pdlSpesialkoder = setOf(Land(landkodeForKosovo), Land(pdlKodeForStatsløs), Land(pdlKodeForUkjentLand))
    private val gyldigeLand = land + pdlSpesialkoder

    fun land(alpha3Code: String): Land {
        require(Land(alpha3Code) in gyldigeLand) {
            "Ugyldig land kode: $alpha3Code"
        }

        return Land(alpha3Code)
    }

    val verden = land

    val tredjeland by lazy { land - norge - storbritannia - eøsEllerSveits }

    val norge = tilLand("NOR", "SJM")

    val storbritannia = tilLand("GBR", "JEY", "IMN")

    val eøsEllerSveits = tilLand(
        "BEL",
        "BGR",
        "DNK",
        "EST",
        "FIN",
        "FRA",
        "GRC",
        "IRL",
        "ISL",
        "ITA",
        "HRV",
        "CYP",
        "LVA",
        "LIE",
        "LTU",
        "LUX",
        "MLT",
        "NLD",
        "POL",
        "PRT",
        "ROU",
        "SVK",
        "SVN",
        "ESP",
        "CHE",
        "SWE",
        "CZE",
        "DEU",
        "HUN",
        "AUT",
    )

    private fun tilLand(vararg land: String) = land.map { this.land(it) }.toSet()
}
