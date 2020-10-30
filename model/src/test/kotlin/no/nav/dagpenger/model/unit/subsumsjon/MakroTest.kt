package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.helpers.testSøknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MakroTest {

    private lateinit var f1: Faktum<Boolean>
    private lateinit var f2: Faktum<Boolean>
    private lateinit var makro: Subsumsjon

    @BeforeEach
    fun setup() {
        val faktagrupper = Fakta(
            ja nei "f1" id 1,
            ja nei "f2" id 2
        ).testSøknad()

        f1 = faktagrupper ja 1
        f2 = faktagrupper ja 2

        val s1 = f1 er true
        val s2 = f2 er true
        makro = "makro" makro (s1 eller s2)
    }

    @Test
    fun `makro resultat er lik child resultat`() {
        assertEquals(null, makro.resultat())
        f1.besvar(true)
        assertEquals(true, makro.resultat())
        f1.besvar(false)
        assertEquals(null, makro.resultat())
        f2.besvar(true)
        assertEquals(true, makro.resultat())
        f2.besvar(false)
        assertEquals(false, makro.resultat())
    }
}
