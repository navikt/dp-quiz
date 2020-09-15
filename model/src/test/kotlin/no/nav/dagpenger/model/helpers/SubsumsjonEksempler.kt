package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.SammensattFaktum
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import java.time.LocalDate

internal lateinit var bursdag67: GrunnleggendeFaktum<LocalDate>
internal lateinit var søknadsdato: GrunnleggendeFaktum<LocalDate>
internal lateinit var ønsketdato: GrunnleggendeFaktum<LocalDate>
internal lateinit var sisteDagMedLønn: GrunnleggendeFaktum<LocalDate>
internal lateinit var inntektSiste3år: GrunnleggendeFaktum<Inntekt>
internal lateinit var inntektSisteÅr: GrunnleggendeFaktum<Inntekt>
internal lateinit var dimisjonsdato: GrunnleggendeFaktum<LocalDate>

internal lateinit var virkningstidspunkt: Faktum<LocalDate>

internal lateinit var inntekt3G: GrunnleggendeFaktum<Inntekt>
internal lateinit var inntekt15G: GrunnleggendeFaktum<Inntekt>

/* ktlint-disable parameter-list-wrapping */
internal fun subsumsjonRoot(): Subsumsjon {
    bursdag67 = GrunnleggendeFaktum<LocalDate>("Datoen du fyller 67")
    søknadsdato = GrunnleggendeFaktum<LocalDate>("Datoen du søker om dagpenger")
    ønsketdato = GrunnleggendeFaktum<LocalDate>("Datoen du ønsker dagpenger fra")
    sisteDagMedLønn = GrunnleggendeFaktum<LocalDate>("Siste dag du mottar lønn")
    inntektSiste3år = GrunnleggendeFaktum<Inntekt>("Inntekt siste 36 måneder")
    inntektSisteÅr = GrunnleggendeFaktum<Inntekt>("Inntekt siste 12 måneder")
    dimisjonsdato = GrunnleggendeFaktum<LocalDate>("Dimisjonsdato")

    virkningstidspunkt = setOf(ønsketdato, søknadsdato, sisteDagMedLønn)
        .faktum("Hvilken dato vedtaket skal gjelde fra", SammensattFaktum<LocalDate>::max)

    inntekt3G = GrunnleggendeFaktum<Inntekt>("3G")
    inntekt15G = GrunnleggendeFaktum<Inntekt>("1.5G")

    return "inngangsvilkår".alle(
        "under67".alle(
            søknadsdato før bursdag67,
            ønsketdato før bursdag67,
            sisteDagMedLønn før bursdag67
        ),
        "virkningstidspunkt er godkjent".alle(
            ønsketdato ikkeFør sisteDagMedLønn,
            søknadsdato ikkeFør sisteDagMedLønn,
        )
    ) så (
        "oppfyller krav til minsteinntekt".minstEnAv(
            inntektSiste3år minst inntekt3G,
            inntektSisteÅr minst inntekt15G,
            dimisjonsdato før virkningstidspunkt
        ) eller "oppfyller ikke kravet til minsteinntekt".alle(
            ønsketdato ikkeFør sisteDagMedLønn
        )
        ) eller "oppfyller ikke inngangsvilkår".alle(
        ønsketdato ikkeFør sisteDagMedLønn
    )
}
