package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.helpers.*
import no.nav.dagpenger.model.helpers.sisteDagMedLønn
import no.nav.dagpenger.model.helpers.søknadsdato
import no.nav.dagpenger.model.helpers.virkningstidspunkt
import no.nav.dagpenger.model.helpers.ønsketdato
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

internal class SammensattFaktum {
    private lateinit var comp: Subsumsjon

    @BeforeEach
    fun setup() {
        comp = subsumsjonRoot()
    }

    @Test
    fun ` `(){
        ønsketdato besvar 2.januar
        søknadsdato besvar 2.januar
        sisteDagMedLønn besvar 1.januar

        //assertEquals(virkningstidspunkt, listOf(ønsketdato, søknadsdato, sisteDagMedLønn).max())
    }
}