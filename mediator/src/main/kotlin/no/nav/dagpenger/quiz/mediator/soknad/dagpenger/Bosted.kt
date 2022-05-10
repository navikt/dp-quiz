package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.erIkke
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.bareEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Bosted : DslFaktaseksjon {
    const val `hvilket land bor du i` = 6001
    const val `reist tilbake etter arbeidsledig` = 6002
    const val `reist tilbake periode` = 6003
    const val `reist tilbake aarsak` = 6004
    const val `reist tilbake en gang eller mer` = 6005
    const val `reist i takt med rotasjon` = 6006

    override val fakta = listOf(
        land faktum "faktum.hvilket-land-bor-du-i" id `hvilket land bor du i`,
        boolsk faktum "faktum.reist-tilbake-etter-arbeidsledig" id `reist tilbake etter arbeidsledig`,
        periode faktum "faktum.reist-tilbake-periode" id `reist tilbake periode`,
        // @todo: Skal denne være tekst?
        tekst faktum "faktum.reist-tilbake-aarsak" id `reist tilbake aarsak`,
        boolsk faktum "faktum.reist-tilbake-en-gang-eller-mer" id `reist tilbake en gang eller mer`,
        boolsk faktum "faktum.reist-i-takt-med-rotasjon" id `reist i takt med rotasjon`,
    )

    override fun seksjon(søknad: Søknad) = listOf(søknad.seksjon("bostedsland", Rolle.søker, *this.databaseIder()))

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad) {

        // TODO: Finn ut av bruk av paragrafer i kode?
        "§ 4-2 Opphold i Norge".bareEnAv(
            `innenfor Norge`(),
            `innenfor Storbritannia`(),
            `innenfor EØS eller Sveits`(),
            `utenfor EØS`()
        )
    }

    private fun Søknad.`innenfor Norge`() = "§ 4-2 Opphold i Norge".bareEnAv(
        *norge().map {
            land(`hvilket land bor du i`).er(it)
        }.toTypedArray()
    )

    private fun Søknad.`innenfor Storbritannia`() = "$ om Storbritannia og brexit ".bareEnAv(
        *storbritannia().map {
            land(`hvilket land bor du i`).er(it)
        }.toTypedArray()
    )

    private fun Søknad.`innenfor EØS eller Sveits`() = "$  innenfor EØS eller Sveits forskrift".bareEnAv(
        *eøsEllerSveits().map {
            land(`hvilket land bor du i`).er(it)
        }.toTypedArray()
    )

    private fun Søknad.`utenfor EØS`() = "$ om resten av verden".alle(
        *(norge() + storbritannia() + eøsEllerSveits()).map { land ->
            land(`hvilket land bor du i`).erIkke(land)
        }.toTypedArray()
    )

    private fun storbritannia() = listOf(Land("GBR"), Land("JEY"), Land("IMN"))

    private fun norge() = listOf(Land("NOR"), Land("SJM"))

    private fun eøsEllerSveits() = listOf(
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
}
