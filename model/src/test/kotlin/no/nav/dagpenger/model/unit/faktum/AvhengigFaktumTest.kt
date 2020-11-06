package no.nav.dagpenger.model.unit.faktum

import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktagrupper.Faktagrupper
import no.nav.dagpenger.model.faktagrupper.Seksjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.regel.er
import no.nav.dagpenger.model.regel.godkjentAv
import no.nav.dagpenger.model.regel.gyldigGodkjentAv
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

internal class AvhengigFaktumTest {
    companion object {
        internal const val UNG_PERSON_FNR_2018 = "12020052345"
        internal val uuid = UUID.randomUUID()
    }
    private val prototypeSøknad = Søknad(
        ja nei "f1" id 1,
        ja nei "approve1" id 2
    )


    @Test
    fun `har avhengigheter`(){
        assertThrows<IllegalArgumentException> {  prototypeSøknad.ja(1) er true gyldigGodkjentAv prototypeSøknad.ja(2) }
    }
}