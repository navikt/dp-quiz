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
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object DokumentasjonskravUtdanning : DslFaktaseksjon {

    const val `dokumentasjon på sluttdato` = 12001
    const val `dokumentasjon på sluttdato tilgjengelig` = 12002
    const val `dokumentasjon på sluttdato ikke tilgjengelig årsak` = 12003

    override val fakta: List<FaktumFactory<*>>
        get() = listOf(
            dokument faktum "faktum.dokument-utdanning-sluttdato" id `dokumentasjon på sluttdato` avhengerAv Utdanning.`avsluttet utdanning siste 6 mnd`,
            boolsk faktum "faktum.dokument-utdanning-sluttdato-tilgjengelig" id `dokumentasjon på sluttdato tilgjengelig`,
            tekst faktum "faktum.dokument-utdanning-sluttdato-årsak" id `dokumentasjon på sluttdato ikke tilgjengelig årsak` avhengerAv `dokumentasjon på sluttdato tilgjengelig`
        )

    override fun seksjon(søknad: Søknad): List<Seksjon> {
        return listOf(søknad.seksjon("dokumentasjonskrav", Rolle.søker, *this.databaseIder()))
    }

    override fun regeltre(søknad: Søknad): DeltreSubsumsjon = with(søknad) {

        "dokumentasjonskrav".deltre {
            "dokumentasjonskrav utdanning".minstEnAv(
                boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`) er false,
                boolsk(Utdanning.`avsluttet utdanning siste 6 mnd`) er true hvisOppfylt {
                    boolsk(`dokumentasjon på sluttdato tilgjengelig`) er true hvisOppfylt {
                        dokument(`dokumentasjon på sluttdato`).utfylt()
                    } hvisIkkeOppfylt {
                        tekst(`dokumentasjon på sluttdato ikke tilgjengelig årsak`).utfylt()
                    }
                }
            )
        }
    }
}
