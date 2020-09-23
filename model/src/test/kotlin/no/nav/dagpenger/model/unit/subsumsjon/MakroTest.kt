package no.nav.dagpenger.model.unit.subsumsjon

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.faktum
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.eller
import no.nav.dagpenger.model.subsumsjon.makro
import no.nav.dagpenger.model.søknad.Seksjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class MakroTest {

    private lateinit var f1: Faktum<Boolean>
    private lateinit var f2: Faktum<Boolean>
    private lateinit var s1: Subsumsjon
    private lateinit var s2: Subsumsjon
    private lateinit var makro: Subsumsjon

    @BeforeEach
    fun setup() {
        f1 = FaktumNavn(1, "f1").faktum(Boolean::class.java)
        f2 = FaktumNavn(2, "f2").faktum(Boolean::class.java)
        Seksjon(Rolle.søker, f1, f2)
        s1 = f1 er true
        s2 = f2 er true
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
