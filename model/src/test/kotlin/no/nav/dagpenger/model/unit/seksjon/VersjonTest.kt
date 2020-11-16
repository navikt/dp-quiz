package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Søknadprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.seksjon.Versjon.UserInterfaceType.Web
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VersjonTest {
    private lateinit var søknadprosess: Søknadprosess
    @BeforeEach
    fun setup() {
        val fnr = "12345678910"
        val prototypeSøknad = Søknad(
            heltall faktum "f15" id 15 genererer 16 og 17 og 18,
            heltall faktum "f16" id 16,
            ja nei "f17" id 17,
            ja nei "f18" id 18
        )
        val prototypeSeksjon = Seksjon("seksjon", Rolle.søker, prototypeSøknad id 15, prototypeSøknad id 16, prototypeSøknad id 17, prototypeSøknad id 18)
        val prototypeFaktagrupper = Søknadprosess(prototypeSeksjon)
        val prototypeSubsumsjon = prototypeSøknad heltall 15 er 6
        val versjon = Versjon(prototypeSøknad, prototypeSubsumsjon, mapOf(Web to prototypeFaktagrupper))
        søknadprosess = versjon.søknadprosess(fnr, Web)
    }

    @Test
    fun ` bygg fra prototype `() {
        søknadprosess.søknad.also { søknad ->
            assertEquals(TemplateFaktum::class, søknad.id(16)::class)
            assertEquals(GeneratorFaktum::class, søknad.id(15)::class)
            assertEquals(4, søknad.size)
            assertEquals(4, søknadprosess[0].size)
            (søknad heltall 15).besvar(2)
            assertEquals(10, søknad.size)
            assertEquals(10, søknadprosess[0].size)
        }
    }

    @Test
    fun `bygg fra fakta`() {
        søknadprosess.heltall(15).besvar(2)
        var nysøknad = søknadprosess.søknad.søknadprosess(Web)
        nysøknad.heltall("16.1").besvar(1)
        assertEquals(1, nysøknad.heltall("16.1").svar())
    }
}
