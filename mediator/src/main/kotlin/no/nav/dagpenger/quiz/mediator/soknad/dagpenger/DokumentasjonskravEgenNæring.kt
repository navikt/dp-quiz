package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object DokumentasjonskravEgenNæring : DslFaktaseksjon {

    const val `dokumentasjon på arbeidstimer for egen næring` = 13001
    const val `dokumentasjon på arbeidstimer for egen næring tilgjengelig` = 13002
    const val `dokumentasjon på arbeidstimer for egen næring ikke tilgjengelig årsak` = 13003
    const val `dokumentasjon på arbeidstimer for gårdsbruk` = 13004
    const val `dokumentasjon på arbeidstimer for gårdsbruk tilgjengelig` = 13005
    const val `dokumentasjon på arbeidstimer for gårdsbruk ikke tilgjengelig årsak` = 13006

    override val fakta: List<FaktumFactory<*>>
        get() = listOf(
            dokument faktum "faktum.dokument-egen-næring-arbeidstimer" id `dokumentasjon på arbeidstimer for egen næring` avhengerAv EgenNæring.`driver du egen naering`,
            boolsk faktum "faktum.dokument-egen-næring-arbeidstimer-tilgjengelig" id `dokumentasjon på arbeidstimer for egen næring tilgjengelig`,
            tekst faktum "faktum.dokument-egen-næring-arbeidstimer-ikke-tilgjengelig-årsak" id `dokumentasjon på arbeidstimer for egen næring ikke tilgjengelig årsak` avhengerAv `dokumentasjon på arbeidstimer for egen næring tilgjengelig`,
            dokument faktum "faktum.dokument-egen-næring-gårdsbruk-arbeidstimer" id `dokumentasjon på arbeidstimer for gårdsbruk` avhengerAv EgenNæring.`driver du eget gaardsbruk`,
            boolsk faktum "faktum-dokument-egen-næring-gårdsbruk-arbeidstimer-tilgjengelig" id `dokumentasjon på arbeidstimer for gårdsbruk tilgjengelig`,
            tekst faktum "faktum.dokument-egen-næring-gårdsbruk-arbeidstimer-ikke-tilgjengelig-årsak" id `dokumentasjon på arbeidstimer for gårdsbruk ikke tilgjengelig årsak` avhengerAv `dokumentasjon på arbeidstimer for gårdsbruk tilgjengelig`
        )

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        return listOf(søknad.seksjon("dokumentasjonskrav", Rolle.søker, *this.databaseIder()))
    }

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {

        "dokumentasjonskrav".deltre {
            "dokumentasjonskrav egen næring".alle(
                `dokumentasjon arbidstimer egen næring`(),
                `dokumentasjon arbeidstimer eget gårdsbruk`()
            )
        }
    }

    private fun Søknad.`dokumentasjon arbidstimer egen næring`() =
        "dokumentasjon eller årsak for manglende dokumentasjon".minstEnAv(
            boolsk(EgenNæring.`driver du egen naering`) er false,
            boolsk(EgenNæring.`driver du egen naering`) er true hvisOppfylt {
                boolsk(`dokumentasjon på arbeidstimer for egen næring tilgjengelig`) er true hvisOppfylt {
                    dokument(`dokumentasjon på arbeidstimer for egen næring`).utfylt()
                } hvisIkkeOppfylt {
                    tekst(`dokumentasjon på arbeidstimer for egen næring ikke tilgjengelig årsak`).utfylt()
                }
            }
        )

    private fun Søknad.`dokumentasjon arbeidstimer eget gårdsbruk`() =
        "dokumentasjon eller årsak for manglende dokumentasjon".minstEnAv(
            boolsk(EgenNæring.`driver du eget gaardsbruk`) er false,
            boolsk(EgenNæring.`driver du eget gaardsbruk`) er true hvisOppfylt {
                boolsk(`dokumentasjon på arbeidstimer for gårdsbruk tilgjengelig`) er true hvisOppfylt {
                    dokument(`dokumentasjon på arbeidstimer for gårdsbruk`).utfylt()
                } hvisIkkeOppfylt {
                    tekst(`dokumentasjon på arbeidstimer for gårdsbruk ikke tilgjengelig årsak`).utfylt()
                }
            }
        )
}
