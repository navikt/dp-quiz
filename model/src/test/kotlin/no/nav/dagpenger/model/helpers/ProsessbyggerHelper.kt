package no.nav.dagpenger.model.helpers

import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Prosess
import java.util.UUID

fun Prosess.testBygger(testPerson: Person): Prosess {
    val fakta = this.fakta.bygg(testPerson)
    val subsumsjon = this.rootSubsumsjon.bygg(fakta)
    return this.bygg(UUID.randomUUID(), fakta, subsumsjon)
}
