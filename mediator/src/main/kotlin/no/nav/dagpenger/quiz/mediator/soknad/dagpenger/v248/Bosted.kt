package no.nav.dagpenger.quiz.mediator.soknad.dagpenger.v248

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Fakta.Companion.seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.erIkke
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.bareEnAv
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.land.Landfabrikken.eøsEllerSveits
import no.nav.dagpenger.quiz.mediator.land.Landfabrikken.norge
import no.nav.dagpenger.quiz.mediator.land.Landfabrikken.storbritannia
import no.nav.dagpenger.quiz.mediator.land.Landfabrikken.tredjeland
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object Bosted : DslFaktaseksjon {
    const val `hvilket land bor du i` = 6001
    const val `reist tilbake etter arbeidsledig` = 6002
    const val `reist tilbake periode` = 6003
    const val `reist tilbake årsak` = 6004
    const val `reist tilbake en gang i uka eller mer` = 6005
    const val `reist i takt med rotasjon` = 6006

    override val fakta = listOf(
        land faktum "faktum.hvilket-land-bor-du-i"
            gruppe "tredjeland" med tredjeland
            gruppe "eøs" med eøsEllerSveits
            gruppe "norge-jan-mayen" med norge
            gruppe "storbritannia" med storbritannia id `hvilket land bor du i`,

        boolsk faktum "faktum.reist-tilbake-etter-arbeidsledig" id `reist tilbake etter arbeidsledig`
            avhengerAv `hvilket land bor du i`,

        periode faktum "faktum.reist-tilbake-periode" id `reist tilbake periode`
            avhengerAv `reist tilbake etter arbeidsledig`,

        tekst faktum "faktum.reist-tilbake-aarsak" id `reist tilbake årsak`
            avhengerAv `reist tilbake etter arbeidsledig`,

        boolsk faktum "faktum.reist-tilbake-en-gang-eller-mer" id `reist tilbake en gang i uka eller mer`
            avhengerAv `hvilket land bor du i`,

        boolsk faktum "faktum.reist-i-takt-med-rotasjon" id `reist i takt med rotasjon`
            avhengerAv `reist tilbake en gang i uka eller mer`,
    )

    override fun seksjon(fakta: Fakta) =
        listOf(fakta.seksjon("bostedsland", Rolle.søker, *spørsmålsrekkefølgeForSøker()))

    override fun regeltre(fakta: Fakta): DeltreSubsumsjon = with(fakta) {
        "bosted".deltre {
            // TODO: Finn ut av bruk av paragrafer i kode?
            "§ 4-2 Opphold i Norge".bareEnAv(
                `innenfor Norge`(),
                `innenfor Storbritannia`(),
                `innenfor EØS eller Sveits`(),
                `utenfor EØS`(),
            )
        }
    }

    private fun Fakta.`innenfor Norge`() = "§ 4-2 Opphold i Norge".bareEnAv(
        *norge.map { land ->
            land(`hvilket land bor du i`).er(land)
        }.toTypedArray(),
    )

    private fun Fakta.`innenfor Storbritannia`() = "§ x.x om Storbritannia og brexit ".bareEnAv(
        *storbritannia.map { land ->
            land(`hvilket land bor du i`).er(land)
        }.toTypedArray(),
    )

    private fun Fakta.`innenfor EØS eller Sveits`() = "§ x.x innenfor EØS eller Sveits forskrift".bareEnAv(
        *eøsEllerSveits.map { land ->
            land(`hvilket land bor du i`).er(land)
        }.toTypedArray(),
    ).hvisOppfylt {
        val `reist tilbake en gang i uka eller mer regel` =
            boolsk(`reist tilbake en gang i uka eller mer`) er true hvisIkkeOppfylt {
                boolsk(`reist i takt med rotasjon`).utfylt()
            }

        val reistTilbake = boolsk(`reist tilbake etter arbeidsledig`) er true hvisOppfylt {
            periode(`reist tilbake periode`).utfylt().hvisOppfylt {
                tekst(`reist tilbake årsak`).utfylt().hvisOppfylt {
                    `reist tilbake en gang i uka eller mer regel`
                }
            }
        } hvisIkkeOppfylt {
            `reist tilbake en gang i uka eller mer regel`
        }

        reistTilbake
    }

    private fun Fakta.`utenfor EØS`() = "§ x.x om resten av verden".alle(
        *(norge + storbritannia + eøsEllerSveits).map { `et EØS-land` ->
            land(`hvilket land bor du i`).erIkke(`et EØS-land`)
        }.toTypedArray(),
    )

    override val spørsmålsrekkefølgeForSøker = listOf(
        `hvilket land bor du i`,
        `reist tilbake etter arbeidsledig`,
        `reist tilbake periode`,
        `reist tilbake årsak`,
        `reist tilbake en gang i uka eller mer`,
        `reist i takt med rotasjon`,
    )
}
