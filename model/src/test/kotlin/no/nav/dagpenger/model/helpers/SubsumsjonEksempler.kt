package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.regel.før
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.alle
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så
import no.nav.dagpenger.model.søknad.Faktagrupper
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.dagpenger.model.søknad.Versjon.Type.Web
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
internal fun eksempelSøknad(): Faktagrupper {
    val prototypeFakta = Fakta(
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
    ).also { fakta ->
        bursdag67 = fakta.dato(1) as GrunnleggendeFaktum<LocalDate>
        søknadsdato = fakta.dato(2) as GrunnleggendeFaktum<LocalDate>
        ønsketdato = fakta.dato(3) as GrunnleggendeFaktum<LocalDate>
        sisteDagMedLønn = fakta.dato(4) as GrunnleggendeFaktum<LocalDate>
        inntektSiste3år = fakta.inntekt(5) as GrunnleggendeFaktum<Inntekt>
        inntektSisteÅr = fakta.inntekt(6) as GrunnleggendeFaktum<Inntekt>
        dimisjonsdato = fakta.dato(7) as GrunnleggendeFaktum<LocalDate>
        virkningstidspunkt = fakta.dato(8)
        inntekt3G = fakta.inntekt(9) as GrunnleggendeFaktum<Inntekt>
        inntekt15G = fakta.inntekt(10) as GrunnleggendeFaktum<Inntekt>
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

    val prototypeWebSøknad = Faktagrupper(
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
        1,
        prototypeFakta,
        prototypeSubsumsjon,
        mapOf(Web to prototypeWebSøknad)
    ).søknad("", Web).also { søknad ->
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
}
