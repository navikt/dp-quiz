package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.FaktaVersjon
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import java.time.LocalDate

internal var bursdag67: GrunnleggendeFaktum<LocalDate>
internal var søknadsdato: GrunnleggendeFaktum<LocalDate>
internal var ønsketdato: GrunnleggendeFaktum<LocalDate>
internal var sisteDagMedLønn: GrunnleggendeFaktum<LocalDate>
internal var inntektSiste3år: GrunnleggendeFaktum<Inntekt>
internal var inntektSisteÅr: GrunnleggendeFaktum<Inntekt>
internal var dimisjonsdato: GrunnleggendeFaktum<LocalDate>
internal var virkningstidspunkt: Faktum<LocalDate>
internal var inntekt3G: GrunnleggendeFaktum<Inntekt>
internal var inntekt15G: GrunnleggendeFaktum<Inntekt>
private val prototypeFakta = Fakta(
    FaktaVersjon(Testprosess.Test, 13),
    dato faktum "Datoen du fyller 67" id 1,
    dato faktum "Datoen du søker om dagpenger" id 2,
    dato faktum "Datoen du ønsker dagpenger fra" id 3,
    dato faktum "Siste dag du mottar lønn" id 4,
    inntekt faktum "Inntekt siste 36 måneder" id 5,
    inntekt faktum "Inntekt siste 12 måneder" id 6,
    dato faktum "Dimisjonsdato" id 7,
    maks dato "Hvilken dato vedtaket skal gjelde fra" av 2 og 3 og 4 id 8,
    inntekt faktum "3G" id 9,
    inntekt faktum "1.5G" id 10
).also { søknad ->
    bursdag67 = søknad.dato(1) as GrunnleggendeFaktum<LocalDate>
    søknadsdato = søknad.dato(2) as GrunnleggendeFaktum<LocalDate>
    ønsketdato = søknad.dato(3) as GrunnleggendeFaktum<LocalDate>
    sisteDagMedLønn = søknad.dato(4) as GrunnleggendeFaktum<LocalDate>
    inntektSiste3år = søknad.inntekt(5) as GrunnleggendeFaktum<Inntekt>
    inntektSisteÅr = søknad.inntekt(6) as GrunnleggendeFaktum<Inntekt>
    dimisjonsdato = søknad.dato(7) as GrunnleggendeFaktum<LocalDate>
    virkningstidspunkt = søknad.dato(8)
    inntekt3G = søknad.inntekt(9) as GrunnleggendeFaktum<Inntekt>
    inntekt15G = søknad.inntekt(10) as GrunnleggendeFaktum<Inntekt>
}

/* ktlint-disable parameter-list-wrapping */
private val prototypeSubsumsjon = "inngangsvilkår".alle(
    "under67".alle(
        søknadsdato før bursdag67,
        ønsketdato før bursdag67,
        sisteDagMedLønn før bursdag67
    ),
    "virkningstidspunkt er godkjent".alle(
        ønsketdato ikkeFør sisteDagMedLønn,
        søknadsdato ikkeFør sisteDagMedLønn,
    )
) hvisOppfylt {
    "oppfyller krav til minsteinntekt".minstEnAv(
        inntektSiste3år minst inntekt3G,
        inntektSisteÅr minst inntekt15G,
        dimisjonsdato før virkningstidspunkt
    ) hvisIkkeOppfylt {
        "oppfyller ikke kravet til minsteinntekt".alle(
            ønsketdato ikkeFør sisteDagMedLønn
        )
    }
} hvisIkkeOppfylt {
    "oppfyller ikke inngangsvilkår".alle(
        ønsketdato ikkeFør sisteDagMedLønn
    )
}
private val prototypeWebSøknad = Utredningsprosess(
    Seksjon(
        "seksjon1",
        Rolle.søker,
        bursdag67,
        søknadsdato,
        ønsketdato,
        sisteDagMedLønn,
        sisteDagMedLønn,
        dimisjonsdato,
        virkningstidspunkt
    ),
    Seksjon(
        "seksjon2",
        Rolle.søker,
        inntekt15G,
        inntekt3G,
        inntektSiste3år,
        inntektSisteÅr
    )
)
private val søknadprosessTestBygger = Versjon.Bygger(
    prototypeFakta,
    prototypeSubsumsjon,
    prototypeWebSøknad,
)

/* ktlint-disable parameter-list-wrapping */
internal fun eksempelSøknad() = søknadprosessTestBygger.utredningsprosess(testPerson).also { søknadprosess ->
    bursdag67 = søknadprosess.dato(1) as GrunnleggendeFaktum<LocalDate>
    søknadsdato = søknadprosess.dato(2) as GrunnleggendeFaktum<LocalDate>
    ønsketdato = søknadprosess.dato(3) as GrunnleggendeFaktum<LocalDate>
    sisteDagMedLønn = søknadprosess.dato(4) as GrunnleggendeFaktum<LocalDate>
    inntektSiste3år = søknadprosess.inntekt(5) as GrunnleggendeFaktum<Inntekt>
    inntektSisteÅr = søknadprosess.inntekt(6) as GrunnleggendeFaktum<Inntekt>
    dimisjonsdato = søknadprosess.dato(7) as GrunnleggendeFaktum<LocalDate>
    virkningstidspunkt = søknadprosess.dato(8)
    inntekt3G = søknadprosess.inntekt(9) as GrunnleggendeFaktum<Inntekt>
    inntekt15G = søknadprosess.inntekt(10) as GrunnleggendeFaktum<Inntekt>
}
