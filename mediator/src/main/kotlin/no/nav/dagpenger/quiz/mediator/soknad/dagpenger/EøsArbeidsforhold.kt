package no.nav.dagpenger.quiz.mediator.soknad.dagpenger

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.land
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.periode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.tekst
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.Søknad.Companion.seksjon
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.har
import no.nav.dagpenger.model.regel.utfylt
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.quiz.mediator.soknad.DslFaktaseksjon

object EøsArbeidsforhold : DslFaktaseksjon {
    const val `eos arbeid siste 36 mnd` = 9001
    const val `eos arbeidsforhold` = 9002
    const val `eos arbeidsforhold arbeidsgivernavn` = 9003
    const val `eos arbeidsforhold land` = 9004
    const val `eos arbeidsforhold personnummer` = 9005
    const val `eos arbeidsforhold varighet` = 9006

    override val fakta = listOf(
        boolsk faktum "faktum.eos-arbeid-siste-36-mnd" id `eos arbeid siste 36 mnd`,
        heltall faktum "faktum.eos-arbeidsforhold" id `eos arbeidsforhold` avhengerAv `eos arbeid siste 36 mnd`
            genererer `eos arbeidsforhold arbeidsgivernavn`
            og `eos arbeidsforhold land`
            og `eos arbeidsforhold personnummer`
            og `eos arbeidsforhold varighet`,
        tekst faktum "faktum.eos-arbeidsforhold.arbeidsgivernavn" id `eos arbeidsforhold arbeidsgivernavn`,
        land faktum "faktum.eos-arbeidsforhold.land" id `eos arbeidsforhold land`,
        tekst faktum "faktum.eos-arbeidsforhold.personnummer" id `eos arbeidsforhold personnummer`,
        periode faktum "faktum.eos-arbeidsforhold.varighet" id `eos arbeidsforhold varighet`
    )

    fun regeltre(søknad: Søknad): Subsumsjon = with(søknad) {
        "Arbeidsforhold i EØS området".alle(
            boolsk(`eos arbeid siste 36 mnd`) er true hvisOppfylt {
                generator(`eos arbeidsforhold`) har "En til flere EØS arbeidsforhold".deltre {
                    tekst(`eos arbeidsforhold arbeidsgivernavn`).utfylt() hvisOppfylt {
                        land(`eos arbeidsforhold land`).utfylt() hvisOppfylt {
                            tekst(`eos arbeidsforhold personnummer`).utfylt() hvisOppfylt {
                                periode(`eos arbeidsforhold varighet`).utfylt()
                            }
                        }
                    }
                }
            }
        )
    }

    override fun seksjon(søknad: Søknad) =
        listOf(søknad.seksjon("eos-arbeidsforhold", Rolle.søker, *this.databaseIder()))
}
