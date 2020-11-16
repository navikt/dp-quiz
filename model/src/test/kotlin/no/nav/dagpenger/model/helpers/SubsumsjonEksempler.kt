package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
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
internal fun eksempelSøknad(): Søknadprosess {
    val prototypeSøknad = Søknad(
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

    val prototypeSubsumsjon = "inngangsvilkår".alle(
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

    val prototypeWebSøknad = Søknadprosess(
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

    return Versjon(
        prototypeSøknad,
        prototypeSubsumsjon,
        mapOf(Web to prototypeWebSøknad)
    ).søknadprosess("", Web).also { søknadprosess ->
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
}
