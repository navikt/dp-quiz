package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.MAKS_DATO
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

val DATOEN_DU_FYLLER_67 = FaktumNavn(1, "Datoen du fyller 67")
val DATOEN_DU_SØKER_OM_DAGPENGER = FaktumNavn(2, "Datoen du søker om dagpenger")
val DATOEN_DU_ØNSKER_DAGPENGER_FRA = FaktumNavn(3, "Datoen du ønsker dagpenger fra")
val SISTE_DAG_DU_MOTTAR_LØNN = FaktumNavn(4, "Siste dag du mottar lønn")
val INNTEKT_SISTE_36_MÅNEDER = FaktumNavn(5, "Inntekt siste 36 måneder")
val INNTEKT_SISTE_12_MÅNEDER = FaktumNavn(6, "Inntekt siste 12 måneder")
val DIMISJONSDATO = FaktumNavn(7, "Dimisjonsdato")
val VIRKNINGSTIDSPUNKT = FaktumNavn(8, "Hvilken dato vedtaket skal gjelde fra")
val INNTEKT3G = FaktumNavn(9, "3G")
val INNTEKT15G = FaktumNavn(10, "1.5G")

/* ktlint-disable parameter-list-wrapping */
internal fun subsumsjonRoot(): Subsumsjon {
    bursdag67 = DATOEN_DU_FYLLER_67.faktum()
    søknadsdato = DATOEN_DU_SØKER_OM_DAGPENGER.faktum()
    ønsketdato = DATOEN_DU_ØNSKER_DAGPENGER_FRA.faktum()
    sisteDagMedLønn = SISTE_DAG_DU_MOTTAR_LØNN.faktum()
    inntektSiste3år = INNTEKT_SISTE_36_MÅNEDER.faktum()
    inntektSisteÅr = INNTEKT_SISTE_12_MÅNEDER.faktum()
    dimisjonsdato = DIMISJONSDATO.faktum()

    virkningstidspunkt = setOf(ønsketdato, søknadsdato, sisteDagMedLønn)
        .faktum(VIRKNINGSTIDSPUNKT, MAKS_DATO)

    inntekt3G = INNTEKT3G.faktum()
    inntekt15G = INNTEKT15G.faktum()

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
