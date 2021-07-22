package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.med
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.subsumsjon.AlleSubsumsjon
import no.nav.dagpenger.model.subsumsjon.DeltreSubsumsjon
import no.nav.dagpenger.model.subsumsjon.deltre
import no.nav.dagpenger.model.subsumsjon.hvisGyldig
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class GenerertSubsumsjonTest {
    private lateinit var søknad: Søknad

    @BeforeEach
    fun setup() {
        søknad = Søknad(
            0,
            boolsk faktum "template" id 1,
            heltall faktum "generator" id 2 genererer 1
        )
    }

    @Test
    fun `Deltre template kan ikke ha gyldig ugyldig stier`() {
        val template = søknad boolsk 1
        val deltre = "deltre template".deltre { template er true } hvisGyldig { template er true }

        assertThrows<IllegalArgumentException> { søknad generator 2 med (deltre as DeltreSubsumsjon) }
    }

    @Test
    fun `Deltre template subsumsjon works`() {
        val deltre = "deltre template".deltre { søknad boolsk 1 er true }
        val subsumsjon = søknad generator 2 med deltre
        val søknadprosess = Søknadprosess(
            søknad,
            Seksjon("seksjon", Rolle.søker, søknad generator 2, søknad boolsk 1),
            rootSubsumsjon = subsumsjon
        )

        søknadprosess.generator(2).besvar(3)
        subsumsjon.resultat()

        assertEquals(3, (subsumsjon[0].gyldig as AlleSubsumsjon).size)

        søknadprosess.generator(2).besvar(2)
        subsumsjon.resultat()

        assertEquals(2, (subsumsjon[0].gyldig as AlleSubsumsjon).size)
    }
}
