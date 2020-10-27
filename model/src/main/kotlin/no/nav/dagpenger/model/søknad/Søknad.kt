package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.fakta.TypedFaktum
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.util.UUID

class Søknad private constructor(
    val fakta: Fakta,
    internal val rootSubsumsjon: Subsumsjon,
    private val uuid: UUID,
    private val seksjoner: MutableList<Seksjon>
) : TypedFaktum by fakta, MutableList<Seksjon> by seksjoner {

    constructor(vararg seksjoner: Seksjon) : this(
        Fakta(),
        TomSubsumsjon,
        UUID.randomUUID(),
        seksjoner.toMutableList()
    )

    internal constructor(fakta: Fakta, vararg seksjoner: Seksjon, rootSubsumsjon: Subsumsjon = TomSubsumsjon) : this(
        fakta,
        rootSubsumsjon,
        UUID.randomUUID(),
        seksjoner.toMutableList()
    )

    init {
        seksjoner.forEach {
            it.søknad(this)
        }
    }

    internal fun add(faktum: Faktum<*>) = fakta.add(faktum)

    internal infix fun idOrNull(faktumId: FaktumId) = fakta.idOrNull(faktumId)

    fun <T : Comparable<T>> faktum(id: String): Faktum<T> = (fakta.id(id) as Faktum<T>)

    fun <T : Comparable<T>> faktum(id: Int): Faktum<T> = (fakta.id(id) as Faktum<T>)

    infix fun nesteSeksjon(subsumsjon: Subsumsjon) = seksjoner.first { subsumsjon.nesteFakta() in it }

    fun nesteSeksjon() = nesteSeksjon(rootSubsumsjon)

    fun accept(visitor: SøknadVisitor) {
        visitor.preVisit(this, uuid)
        seksjoner.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }

    internal fun faktum(id: FaktumId) = fakta.id(id)

    fun seksjon(navn: String) = seksjoner.first { it.navn == navn }

    internal fun bygg(fakta: Fakta, subsumsjon: Subsumsjon) =
        Søknad(fakta, subsumsjon, UUID.randomUUID(), seksjoner.map { it.bygg(fakta) }.toMutableList())

    internal fun nesteFakta() = rootSubsumsjon.nesteFakta()

    fun resultat() = rootSubsumsjon.resultat()
}
