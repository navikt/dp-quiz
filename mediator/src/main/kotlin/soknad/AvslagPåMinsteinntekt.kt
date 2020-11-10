package soknad

import db.FaktumTable
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktagrupper.Versjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.av
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.ikkeFør
import no.nav.dagpenger.model.regel.minst
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.subsumsjon.minstEnAv
import no.nav.dagpenger.model.subsumsjon.så

// Forstår dagpengesøknaden
internal class AvslagPåMinsteinntekt {

    init {
        val faktumRecord: FaktumTable = FaktumTable(søknad, VERSJON_ID)
    }

    private companion object {
        const val VERSJON_ID = 2
    }

    private val søknad: Søknad
        get() = Søknad(
            dato faktum "Ønsker dagpenger fra dato" id 1,
            dato faktum "Siste dag med arbeidsplikt" id 2,
            dato faktum "Registreringsdato" id 3,
            dato faktum "Siste dag med lønn" id 4,
            maks dato "Virkningstidspunkt" av 1 og 2 og 3 og 4 og 11 id 5,
            ja nei "Driver med fangst og fisk" id 6,
            inntekt faktum "Inntekt siste 3 år" id 7 avhengerAv 5 og 6,
            inntekt faktum "Inntekt siste 12 mnd" id 8 avhengerAv 5 og 6,
            inntekt faktum "3G" id 9,
            inntekt faktum "1,5G" id 10,
            dato faktum "Søknadstidspunkt" id 11,
            ja nei "Verneplikt" id 12,
            ja nei "Godjenning av virkingstidspunkt" id 13 avhengerAv 1 og 2 og 3 og 4 og 11, //denne bør avhenge av id 5, men dette fungerer ikke per i dag
            dokument faktum "dokumentasjon for fangst og fisk" id 14 avhengerAv 6,
            ja nei "Godkjenning av dokumentasjon for fangst og fisk" id 15 avhengerAv 14,
        )
    private val ønsketDato = søknad dato 1
    private val registreringsdato = søknad dato 2
    private val sisteDagMedLønn = søknad dato 3
    private val sisteDagMedArbeidsplikt = søknad dato 4
    private val virkningstidspunkt = søknad dato 5
    private val fangstOgFisk = søknad ja 6
    private val inntektSiste3År = søknad inntekt 7
    private val inntektSisteÅr = søknad inntekt 8
    private val inntekt3G = søknad inntekt 9
    private val inntekt15G = søknad inntekt 10
    private val søknadstidspunkt = søknad dato 11
    private val verneplikt = søknad ja 12
    private val godkjenningVirkningstidspunkt = søknad ja 13
    private val dokumentasjonFangstOgFisk = søknad dokument 14
    private val godkjenningFangstOgFisk = søknad ja 15

    private val minsteArbeidsinntekt = "minste arbeidsinntekt".minstEnAv(
        inntektSiste3År minst inntekt3G,
        inntektSisteÅr minst inntekt15G,
        verneplikt er true
    )

    private val sjekkFangstOgFisk = "fangst og fisk er dokumentert" makro (
        fangstOgFisk er false eller (godkjenningFangstOgFisk av dokumentasjonFangstOgFisk)
        )

    private val inngangsvilkår =
        (
            (søknadstidspunkt ikkeFør virkningstidspunkt)
                godkjentAv godkjenningVirkningstidspunkt
            ) så (
            sjekkFangstOgFisk så (
                minsteArbeidsinntekt
                )
            )
    private val statiske =
        Seksjon(
            "statiske",
            Rolle.nav,
            inntekt3G,
            inntekt15G,
        )
    private val datoer =
        Seksjon(
            "datoer",
            Rolle.nav,
            ønsketDato,
            søknadstidspunkt,
            registreringsdato,
            sisteDagMedArbeidsplikt,
            sisteDagMedLønn
        )
    private val vernepliktSeksjon =
        Seksjon(
            "verneplikt",
            Rolle.nav,
            verneplikt,
        )
    private val egenNæring =
        Seksjon(
            "egenNæring",
            Rolle.nav,
            fangstOgFisk,
        )
    private val fangstOgFiskSeksjon =
        Seksjon(
            "egenNæring",
            Rolle.nav,
            dokumentasjonFangstOgFisk
        )
    private val inntekter =
        Seksjon(
            "inntekter",
            Rolle.nav,
            inntektSisteÅr,
            inntektSiste3År,
        )
    private val godkjennDato =
        Seksjon(
            "godkjenn virkningstidspunkt",
            Rolle.saksbehandler,
            godkjenningVirkningstidspunkt
        )
    internal val faktagrupper: Faktagrupper =
        Faktagrupper(
            statiske,
            datoer,
            vernepliktSeksjon,
            egenNæring,
            fangstOgFiskSeksjon,
            inntekter,
            godkjennDato
        )
    private val versjon = Versjon(
        versjonId = VERSJON_ID,
        prototypeSøknad = søknad,
        prototypeSubsumsjon = inngangsvilkår,
        prototypeFaktagrupper = mapOf(
            Versjon.FaktagrupperType.Web to faktagrupper
        )
    )

    fun faktagrupper(fnr: String) = versjon.faktagrupper(fnr, Versjon.FaktagrupperType.Web)
}
