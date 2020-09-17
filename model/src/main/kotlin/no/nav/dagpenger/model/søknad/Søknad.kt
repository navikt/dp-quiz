package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import java.util.UUID

class Søknad private constructor(uuid: UUID, private val seksjoner: List<Seksjon>) : Collection<Seksjon> by seksjoner {
    constructor(vararg seksjoner: Seksjon) : this(UUID.randomUUID(), seksjoner.toList())
    infix fun nesteSeksjon(subsumsjon: Subsumsjon) = seksjoner.first { subsumsjon.nesteFakta() in it }
}
