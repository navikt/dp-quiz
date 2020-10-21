package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.util.UUID

class Søknad private constructor(
    internal val fakta: Fakta,
    private val rootSubsumsjon: Subsumsjon,
    private val uuid: UUID,
    private val seksjoner: MutableList<Seksjon>
) : MutableList<Seksjon> by seksjoner {

    constructor(vararg seksjoner: Seksjon) : this(
        Fakta("", mutableMapOf()),
        TomSubsumsjon,
        UUID.randomUUID(),
        seksjoner.toMutableList()
    )

    internal constructor(fakta: Fakta, rootSubsumsjon: Subsumsjon, vararg seksjoner: Seksjon) : this(
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

    fun <T : Comparable<T>> faktum(id: String): Faktum<T> = (fakta.id(id) as Faktum<T>)

    fun <T : Comparable<T>> faktum(id: Int): Faktum<T> = (fakta.id(id) as Faktum<T>)

    fun ja(id: Int) = fakta.ja(id)

    fun dato(id: Int) = fakta.dato(id)

    fun dokument(id: Int) = fakta.dokument(id)

    fun inntekt(id: Int) = fakta.inntekt(id)

    fun heltall(id: Int) = fakta.heltall(id)

    internal infix fun nesteSeksjon(subsumsjon: Subsumsjon) = seksjoner.first { subsumsjon.nesteFakta() in it }

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

}