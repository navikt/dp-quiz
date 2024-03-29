package no.nav.dagpenger.model.unit.seksjon

import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.helpers.faktaversjon
import no.nav.dagpenger.model.helpers.testProsess
import no.nav.dagpenger.model.helpers.testProsesstype
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Seksjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

internal class ProsessversjonTest {
    private lateinit var prosess: Prosess

    private companion object {
        val prosesstype = testProsesstype()
        val prototypeFakta = Fakta(
            prosesstype.faktaversjon,
            heltall faktum "f15" id 15 genererer 16 og 17 og 18,
            heltall faktum "f16" id 16,
            boolsk faktum "f17" id 17,
            boolsk faktum "f18" id 18,
        )
        val prototypeSeksjon = Seksjon(
            "seksjon",
            Rolle.søker,
            prototypeFakta id 15,
            prototypeFakta id 16,
            prototypeFakta id 17,
            prototypeFakta id 18,
        )
        val prototypeSubsumsjon = prototypeFakta heltall 15 er 6
        val prototypeProsess = Prosess(
            prosesstype,
            prototypeFakta,
            prototypeSeksjon,
            rootSubsumsjon = prototypeSubsumsjon,
        )
    }

    @BeforeEach
    fun setup() {
        prosess = prototypeProsess.testProsess()
    }

    @Test
    fun ` bygg fra prototype `() {
        prosess.fakta.also { søknad ->
            assertEquals(TemplateFaktum::class, søknad.id(16)::class)
            assertEquals(GeneratorFaktum::class, søknad.id(15)::class)
            assertEquals(4, søknad.size)
            assertEquals(4, prosess[0].size)
            (søknad heltall 15).besvar(2)
            assertEquals(10, søknad.size)
            assertEquals(10, prosess[0].size)
        }
    }
}
