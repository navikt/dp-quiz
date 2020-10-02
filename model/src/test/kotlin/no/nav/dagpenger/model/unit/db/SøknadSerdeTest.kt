package no.nav.dagpenger.model.unit.db

import no.nav.dagpenger.model.db.SøknadBuilder
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.SøknadJsonBuilder
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class SøknadSerdeTest {

    @Test
    @Disabled
    fun test() {
        val faktum: Faktum<*> = FaktumNavn(1, "faktum").faktum(Boolean::class.java)
        val seksjon = Seksjon(Rolle.søker, faktum)
        val originalSøknad = Søknad(seksjon)

        val originalJson = SøknadJsonBuilder(originalSøknad).resultat()
        val builder = SøknadBuilder(originalJson.toString())
        val nySøknad = builder.resultat()

        assertEquals(originalSøknad.size, nySøknad.size)
        assertEquals(originalJson, SøknadJsonBuilder(nySøknad).resultat())
    }
}
