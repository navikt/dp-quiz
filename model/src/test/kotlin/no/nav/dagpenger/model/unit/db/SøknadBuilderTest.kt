package no.nav.dagpenger.model.unit.db

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.UtledetFaktumFactory
import no.nav.dagpenger.model.factory.UtledetFaktumFactory.Companion.maks
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.fakta.template
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.MAKS_DATO
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SøknadBuilderTest {

    @Test
    fun `Gjenopprette søknad med fakta uten svar`() {
        val fakta = Fakta(
                ja nei "boolean" id 1,
                heltall faktum "int" id 2,
                inntekt faktum "inntekt" id 3,
                dato faktum "dato" id 4,
                dokument faktum "dokument" id 5
        )

        val seksjon1 = Seksjon("seksjon1", Rolle.søker, fakta ja 1, fakta inntekt 3, fakta dokument 5)
        val seksjon2 = Seksjon("seksjon2", Rolle.saksbehandler, fakta heltall 2, fakta dato 4)

        assert(Søknad(seksjon1, seksjon2))
    }

    @Test
    fun `Gjenopprette søknad med fakta og svar`() {
        val fakta = Fakta(
            ja nei "boolean" id 1,
            heltall faktum "int" id 2,
            inntekt faktum "inntekt" id 3,
            dato faktum "dato" id 4,
            dokument faktum "dokument" id 5
         )

        val seksjon1 = Seksjon("seksjon1", Rolle.søker, fakta ja 1, fakta inntekt 3, fakta dokument 5)
        val seksjon2 = Seksjon("seksjon2", Rolle.saksbehandler, fakta heltall 2, fakta dato 4)

        val søknad = Søknad(fakta, seksjon1, seksjon2)

        søknad.ja(1).besvar(true, Rolle.søker)
        søknad.heltall(2).besvar(5, Rolle.saksbehandler)
        søknad.inntekt(3).besvar(20000.årlig, Rolle.søker)
        søknad.dato(4).besvar(1.januar, Rolle.saksbehandler)
        søknad.dokument(5).besvar(Dokument(2.januar), Rolle.søker)

        assert(søknad)
    }

    @Test
    fun `Gjenopprette søknad med utledet faktum`() {
        val fakta = Fakta(
                dato faktum "dato" id 1,
                dato faktum "dato" id 2,
                maks dato "utledet" av 1 og 2 id 3
        )

        val seksjon1 = Seksjon("seksjon1", Rolle.søker, fakta dato 1, fakta dato 2)
        val seksjon2 = Seksjon("seksjon2", Rolle.søker, fakta dato 3)
        assert(Søknad(seksjon1, seksjon2))
    }

    @Test
    fun `Gjenopprette søknad med utsatt faktabygging`() {
        val fakta = Fakta(
                dato faktum "dato" id 1,
                dato faktum "dato" id 2,
                maks dato "utledet" av 1 og 2 id 3
        )

        val seksjon1 = Seksjon("seksjon1", Rolle.søker, fakta dato 3)
        val seksjon2 = Seksjon("seksjon2", Rolle.søker, fakta dato 1, fakta dato 2)
        assert(Søknad(seksjon1, seksjon2))
    }

    @Test
    fun `Gjenopprette søknad med nøsta utledet faktum`() {
        val fakta = Fakta(
                dato faktum "dato" id 1,
                dato faktum "dato" id 2,
                maks dato "utledet 1" av 1 og 2 id 3,
                maks dato "utledet 2" av 1 og 3 id 4
        )

        val seksjon1 = Seksjon("seksjon1", Rolle.søker, fakta dato 4)
        val seksjon2 = Seksjon("seksjon2", Rolle.søker, fakta dato 2, fakta dato 3)
        val seksjon3 = Seksjon("seksjon3", Rolle.søker, fakta dato 1, fakta dato 2)
        assert(Søknad(seksjon1, seksjon2, seksjon3))
    }

    @Test
    fun `Hvilken som helst bokstav vil fungere`() {
        val fakta = Fakta(
                heltall faktum "generator" genererer 2 id 1,
                heltall faktum "template" id 2
        )

        val seksjon = Seksjon("seksjon1", Rolle.søker, fakta heltall 1, fakta heltall 2)
        assert(Søknad(seksjon))
    }

    private fun assert(originalSøknad: Søknad) {
//        val originalJson = SøknadJsonBuilder(originalSøknad).resultat()
//        val builder = SøknadBuilder(originalJson.toString())
//        val nySøknad = builder.resultat()
//
//        assertEquals(originalSøknad.size, nySøknad.size)
//        assertEquals(originalJson, SøknadJsonBuilder(nySøknad).resultat())
//        assertDeepEquals(originalSøknad, nySøknad)
    }
}
