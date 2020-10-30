package no.nav.dagpenger.model.unit.søknad

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.søknad.Faktagrupper
import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Versjon
import no.nav.dagpenger.model.søknad.Versjon.Type.Web
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VersjonTest {
    private lateinit var faktagrupper: Faktagrupper
    @BeforeEach
    fun setup() {
        val fnr = "12345678910"
        val prototypeFakta = Fakta(
            heltall faktum "f15" id 15 genererer 16 og 17 og 18,
            heltall faktum "f16" id 16,
            ja nei "f17" id 17,
            ja nei "f18" id 18
        )
        val prototypeSeksjon = Seksjon("seksjon", Rolle.søker, prototypeFakta id 15, prototypeFakta id 16, prototypeFakta id 17, prototypeFakta id 18)
        val prototypeSøknad = Faktagrupper(prototypeSeksjon)
        val prototypeSubsumsjon = prototypeFakta heltall 15 er 6
        val versjon = Versjon(1, prototypeFakta, prototypeSubsumsjon, mapOf(Web to prototypeSøknad))
        faktagrupper = versjon.søknad(fnr, Web)
    }

    @Test
    fun ` bygg fra prototype `() {
        faktagrupper.fakta.also { fakta ->
            assertEquals(TemplateFaktum::class, fakta.id(16)::class)
            assertEquals(GeneratorFaktum::class, fakta.id(15)::class)
            assertEquals(4, fakta.size)
            assertEquals(4, faktagrupper[0].size)
            (fakta heltall 15).besvar(2, Rolle.søker)
            assertEquals(10, fakta.size)
            assertEquals(10, faktagrupper[0].size)
        }
    }

    @Test
    fun `bygg fra fakta`() {
        faktagrupper.heltall(15).besvar(2, Rolle.søker)
        var nysøknad = faktagrupper.fakta.søknad(Web)
        nysøknad.heltall("16.1").besvar(1, Rolle.søker)
        assertEquals(1, nysøknad.heltall("16.1").svar())
    }
}
