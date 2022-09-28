package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.faktum.Land

internal fun storbritannia() = listOf(Land("GBR"), Land("JEY"), Land("IMN"))

internal fun norge() = listOf(Land("NOR"), Land("SJM"))

internal fun eøsEllerSveits() = listOf(
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
    "AUT"
).map { land ->
    Land(land)
}
