package no.nav.dagpenger.qamodel.helpers

import no.nav.dagpenger.qamodel.fakta.Faktum
import no.nav.dagpenger.qamodel.port.Inntekt
import no.nav.dagpenger.qamodel.subsumsjon.Subsumsjon
import no.nav.dagpenger.qamodel.subsumsjon.alle
import no.nav.dagpenger.qamodel.subsumsjon.eller
import no.nav.dagpenger.qamodel.subsumsjon.etter
import no.nav.dagpenger.qamodel.subsumsjon.før
import no.nav.dagpenger.qamodel.subsumsjon.ikkeFør
import no.nav.dagpenger.qamodel.subsumsjon.minst
import no.nav.dagpenger.qamodel.subsumsjon.minstEnAv
import no.nav.dagpenger.qamodel.subsumsjon.så
import java.time.LocalDate

internal lateinit var bursdag67: Faktum<LocalDate>
internal lateinit var søknadsdato: Faktum<LocalDate>
internal lateinit var ønsketdato: Faktum<LocalDate>
internal lateinit var sisteDagMedLønn: Faktum<LocalDate>
internal lateinit var inntektSiste3år: Faktum<Inntekt>
internal lateinit var inntektSisteÅr: Faktum<Inntekt>
internal lateinit var dimisjonsdato: Faktum<LocalDate>

internal lateinit var virkningstidspunkt: Faktum<LocalDate>

internal lateinit var inntekt3G: Faktum<Inntekt>
internal lateinit var inntekt15G: Faktum<Inntekt>

/* ktlint-disable parameter-list-wrapping */
internal fun subsumsjonRoot(): Subsumsjon {
    bursdag67 = Faktum<LocalDate>("Datoen du fyller 67")
    søknadsdato = Faktum<LocalDate>("Datoen du søker om dagpenger")
    ønsketdato = Faktum<LocalDate>("Datoen du ønsker dagpenger fra")
    sisteDagMedLønn = Faktum<LocalDate>("Siste dag du mottar lønn")
    inntektSiste3år = Faktum<Inntekt>("Inntekt siste 36 måneder")
    inntektSisteÅr = Faktum<Inntekt>("Inntekt siste 12 måneder")
    dimisjonsdato = Faktum<LocalDate>("Dimisjonsdato")

    virkningstidspunkt = Faktum<LocalDate>("Hvilken dato vedtaket skal gjelde fra")

    inntekt3G = Faktum<Inntekt>("3G")
    inntekt15G = Faktum<Inntekt>("1.5G")

    return "inngangsvilkår".alle(
            "under67".alle(
                    søknadsdato før bursdag67,
                    ønsketdato før bursdag67,
                    sisteDagMedLønn før bursdag67
            ),
            "kravdato er godkjent".alle(
                    ønsketdato ikkeFør sisteDagMedLønn,
                    søknadsdato ikkeFør sisteDagMedLønn,
            )
    ) så (
            "oppfyller krav til minsteinntekt".minstEnAv(
                    inntektSiste3år minst inntekt3G,
                    inntektSisteÅr minst inntekt15G,
                    dimisjonsdato etter virkningstidspunkt
            ) eller "oppfyller ikke kravet til minsteinntekt".alle(
                    ønsketdato ikkeFør sisteDagMedLønn
            )
            ) eller "oppfyller ikke inngangsvilkår".alle(
            ønsketdato ikkeFør sisteDagMedLønn
    )
}
