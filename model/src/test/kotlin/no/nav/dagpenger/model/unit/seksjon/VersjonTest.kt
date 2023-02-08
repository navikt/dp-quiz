package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.helpers.Testprosess
import no.nav.dagpenger.model.helpers.testPerson
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class VersjonTest {
    private lateinit var utredningsprosess: Utredningsprosess

    private companion object {
        val prototypeFakta = Fakta(
            Faktaversjon(Testprosess.Test, 1),
            heltall faktum "f15" id 15 genererer 16 og 17 og 18,
            heltall faktum "f16" id 16,
            boolsk faktum "f17" id 17,
            boolsk faktum "f18" id 18,
        )
        val prototypeSeksjon = Seksjon("seksjon", Rolle.søker, prototypeFakta id 15, prototypeFakta id 16, prototypeFakta id 17, prototypeFakta id 18)
        val prototypeUtredningsprosess = Utredningsprosess(prototypeSeksjon)
        val prototypeSubsumsjon = prototypeFakta heltall 15 er 6
        val versjon = Versjon.Bygger(
            prototypeFakta,
            prototypeSubsumsjon,
            prototypeUtredningsprosess,
        ).registrer()
    }

    @BeforeEach
    fun setup() {
        utredningsprosess = versjon.utredningsprosess(testPerson)
    }

    @Test
    fun ` bygg fra prototype `() {
        utredningsprosess.fakta.also { søknad ->
            assertEquals(TemplateFaktum::class, søknad.id(16)::class)
            assertEquals(GeneratorFaktum::class, søknad.id(15)::class)
            assertEquals(4, søknad.size)
            assertEquals(4, utredningsprosess[0].size)
            (søknad heltall 15).besvar(2)
            assertEquals(10, søknad.size)
            assertEquals(10, utredningsprosess[0].size)
        }
    }
}
