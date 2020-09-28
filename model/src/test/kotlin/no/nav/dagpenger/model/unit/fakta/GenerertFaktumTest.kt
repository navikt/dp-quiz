package no.nav.dagpenger.model.unit.fakta

import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test


internal class GenerertFaktumTest {

    @Test
    fun test(){
        val template = FaktumNavn(1, "template").template(Boolean::class.java)
        val generator = FaktumNavn(2, "generer").faktum(Int::class.java, template)
        val seksjon = Seksjon(Rolle.søker, template, generator)
        val originalSize = seksjon.size
        generator.besvar(5)

        assertEquals(5, seksjon.size - originalSize)
    }
}

