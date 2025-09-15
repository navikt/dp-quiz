package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.hvisIkkeOppfylt
import no.nav.dagpenger.model.subsumsjon.hvisOppfylt
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import java.time.LocalDate

class EksempelSøknad {
    private var bursdag67: GrunnleggendeFaktum<LocalDate>
    private var søknadsdato: GrunnleggendeFaktum<LocalDate>
    private var ønsketdato: GrunnleggendeFaktum<LocalDate>
    private var sisteDagMedLønn: GrunnleggendeFaktum<LocalDate>
    private var inntektSiste3år: GrunnleggendeFaktum<Inntekt>
    private var inntektSisteÅr: GrunnleggendeFaktum<Inntekt>
    private var dimisjonsdato: GrunnleggendeFaktum<LocalDate>
    private var virkningstidspunkt: Faktum<LocalDate>
    private var inntekt3G: GrunnleggendeFaktum<Inntekt>
    private var inntekt15G: GrunnleggendeFaktum<Inntekt>
    private val prosesstype = testProsesstype()
    private val prototypeFakta =
        Fakta(
            prosesstype.faktaversjon,
            dato faktum "Datoen du fyller 67" id 1,
            dato faktum "Datoen du søker om dagpenger" id 2,
            dato faktum "Datoen du ønsker dagpenger fra" id 3,
            dato faktum "Siste dag du mottar lønn" id 4,
            inntekt faktum "Inntekt siste 36 måneder" id 5,
            inntekt faktum "Inntekt siste 12 måneder" id 6,
            dato faktum "Dimisjonsdato" id 7,
            maks dato "Hvilken dato vedtaket skal gjelde fra" av 2 og 3 og 4 id 8,
            inntekt faktum "3G" id 9,
            inntekt faktum "1.5G" id 10,
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
    private val prototypeSubsumsjon =
        "inngangsvilkår".alle(
            "under67".alle(
                søknadsdato før bursdag67,
                ønsketdato før bursdag67,
                sisteDagMedLønn før bursdag67,
            ),
            "virkningstidspunkt er godkjent".alle(
                ønsketdato ikkeFør sisteDagMedLønn,
                søknadsdato ikkeFør sisteDagMedLønn,
            ),
        ) hvisOppfylt {
            "oppfyller krav til minsteinntekt".minstEnAv(
                inntektSiste3år minst inntekt3G,
                inntektSisteÅr minst inntekt15G,
                dimisjonsdato før virkningstidspunkt,
            ) hvisIkkeOppfylt {
                "oppfyller ikke kravet til minsteinntekt".alle(
                    ønsketdato ikkeFør sisteDagMedLønn,
                )
            }
        } hvisIkkeOppfylt {
            "oppfyller ikke inngangsvilkår".alle(
                ønsketdato ikkeFør sisteDagMedLønn,
            )
        }
    private val prototypeProsess =
        Prosess(
            prosesstype,
            prototypeFakta,
            Seksjon(
                "seksjon1",
                Rolle.søker,
                bursdag67,
                søknadsdato,
                ønsketdato,
                sisteDagMedLønn,
                sisteDagMedLønn,
                dimisjonsdato,
                virkningstidspunkt,
            ),
            Seksjon(
                "seksjon2",
                Rolle.søker,
                inntekt15G,
                inntekt3G,
                inntektSiste3år,
                inntektSisteÅr,
            ),
            rootSubsumsjon = prototypeSubsumsjon,
        )
    internal val prosess by lazy {
        prototypeProsess.testProsess()
    }

    data class FaktaGetters(
        val bursdag67: Faktum<LocalDate>,
        val søknadsdato: Faktum<LocalDate>,
        val ønsketdato: Faktum<LocalDate>,
        val sisteDagMedLønn: Faktum<LocalDate>,
        val inntektSiste3år: Faktum<Inntekt>,
        val inntektSisteÅr: Faktum<Inntekt>,
        val dimisjonsdato: Faktum<LocalDate>,
        val virkningstidspunkt: Faktum<LocalDate>,
        val inntekt3G: Faktum<Inntekt>,
        val inntekt15G: Faktum<Inntekt>,
    )

    fun withProsess(block: FaktaGetters.() -> Unit) {
        val fakta =
            FaktaGetters(
                bursdag67 = prosess.dato(1),
                søknadsdato = prosess.dato(2),
                ønsketdato = prosess.dato(3),
                sisteDagMedLønn = prosess.dato(4),
                inntektSiste3år = prosess.inntekt(5),
                inntektSisteÅr = prosess.inntekt(6),
                dimisjonsdato = prosess.dato(7),
                virkningstidspunkt = prosess.dato(8),
                inntekt3G = prosess.inntekt(9),
                inntekt15G = prosess.inntekt(10),
            )
        fakta.block()
    }
}
