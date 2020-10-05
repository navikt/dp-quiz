package no.nav.dagpenger.model.unit.db

import no.nav.dagpenger.model.db.SøknadBuilder
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Inntekt.Companion.årlig
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.helpers.januar
import no.nav.dagpenger.model.regel.MAKS_DATO
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SøknadJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class SøknadSerdeTest {

    @Test
    fun `Gjenopprette søknad med fakta uten svar`() {
        val faktumBoolean = FaktumNavn(1, "faktumBoolean").faktum(Boolean::class.java)
        val faktumInt = FaktumNavn(2, "faktumInt").faktum(Int::class.java)
        val faktumInntekt = FaktumNavn(3, "faktumInntekt").indeks(1).faktum(Inntekt::class.java)
        val faktumLocalDate = FaktumNavn(4, "faktumLocalDate").faktum(LocalDate::class.java)
        val faktumDokument = FaktumNavn(5, "faktumDokumet").faktum(Dokument::class.java)

        val seksjon1 = Seksjon(Rolle.søker, faktumBoolean, faktumInntekt, faktumDokument)
        val seksjon2 = Seksjon(Rolle.saksbehandler, faktumInt, faktumLocalDate)

        assert(Søknad(seksjon1, seksjon2))
    }

    @Test
    fun `Gjenopprette søknad med fakta og svar`() {
        val faktumBoolean = FaktumNavn(1, "faktumBoolean").faktum(Boolean::class.java)
        val faktumInt = FaktumNavn(2, "faktumInt").faktum(Int::class.java)
        val faktumInntekt = FaktumNavn(3, "faktumInntekt").indeks(1).faktum(Inntekt::class.java)
        val faktumLocalDate = FaktumNavn(4, "faktumLocalDate").faktum(LocalDate::class.java)
        val faktumDokument = FaktumNavn(5, "faktumDokumet").faktum(Dokument::class.java)

        val seksjon1 = Seksjon(Rolle.søker, faktumBoolean, faktumInntekt, faktumDokument)
        val seksjon2 = Seksjon(Rolle.saksbehandler, faktumInt, faktumLocalDate)

        val søknad = Søknad(seksjon1, seksjon2)

        faktumBoolean.besvar(true, Rolle.søker)
        faktumInt.besvar(5, Rolle.saksbehandler)
        faktumInntekt.besvar(20000.årlig, Rolle.søker)
        faktumLocalDate.besvar(1.januar, Rolle.saksbehandler)
        faktumDokument.besvar(Dokument(2.januar), Rolle.søker)

        assert(søknad)
    }

    @Test
    fun `Gjenopprette søknad med utledet faktum`() {
        val faktum1 = FaktumNavn(1, "f1").faktum(LocalDate::class.java)
        val faktum2 = FaktumNavn(2, "f2").faktum(LocalDate::class.java)

        val utlededFaktum = listOf(faktum1, faktum2).faktum(FaktumNavn(3, "utledet"), MAKS_DATO)

        val seksjon1 = Seksjon(Rolle.søker, faktum1, faktum2)
        val seksjon2 = Seksjon(Rolle.søker, utlededFaktum)
        assert(Søknad(seksjon1, seksjon2))
    }

    @Test
    fun `Gjenopprette søknad med utsatt faktabygging`() {
        val faktum1 = FaktumNavn(1, "f1").faktum(LocalDate::class.java)
        val faktum2 = FaktumNavn(2, "f2").faktum(LocalDate::class.java)

        val utlededFaktum = listOf(faktum1, faktum2).faktum(FaktumNavn(3, "utledet"), MAKS_DATO)

        val seksjon1 = Seksjon(Rolle.søker, utlededFaktum)
        val seksjon2 = Seksjon(Rolle.søker, faktum1, faktum2)
        assert(Søknad(seksjon1, seksjon2))
    }

    private fun assert(originalSøknad: Søknad) {
        val originalJson = SøknadJsonBuilder(originalSøknad).resultat()
        val builder = SøknadBuilder(originalJson.toString())
        val nySøknad = builder.resultat()

        assertEquals(originalSøknad.size, nySøknad.size)
        assertEquals(originalJson, SøknadJsonBuilder(nySøknad).resultat())
    }
}
