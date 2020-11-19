package no.nav.dagpenger.model.unit.marshalling

import com.fasterxml.jackson.databind.node.ObjectNode
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.helpers.testSøknadprosess
import no.nav.dagpenger.model.marshalling.FaktumJsonBuilder
import no.nav.dagpenger.model.visitor.SøknadVisitor
import org.junit.jupiter.api.Test

internal class FaktumJsonBuilderTest {
    @Test
    fun ` `() {
        val søknad = Søknad(
            189,
            ja nei "f1" id 1 avhengerAv 2 og 3,
            ja nei "f2" id 2 avhengerAv 1 og 3,
            ja nei "f3" id 3 avhengerAv 1 og 2
        ).testSøknadprosess().søknad

        val json = TestFaktumJsonBuilder(søknad).resultat()
    }

    private class TestFaktumJsonBuilder(søknad: Søknad) : SøknadVisitor, FaktumJsonBuilder() {
        init {
            søknad.accept(this)
        }

        override fun resultat(): ObjectNode = mapper.createObjectNode().also {
            it.set("fakta", faktaNode)
        }
    }
}
