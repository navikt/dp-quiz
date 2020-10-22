package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.factory.FaktumFactory
import java.time.LocalDate
import java.util.UUID

class Fakta private constructor(
    fnr: String,
    uuid: UUID,
    private val faktumMap: MutableMap<FaktumId, Faktum<*>>
) : TypedFaktum, MutableList<Faktum<*>> by faktumMap.values.toMutableList() {

    internal constructor(fnr: String, faktumMap: MutableMap<FaktumId, Faktum<*>>) : this(
        fnr,
        UUID.randomUUID(),
        faktumMap
    )

    constructor(vararg factories: FaktumFactory<*>) : this(
        "",
        UUID.randomUUID(),
        factories.map {
            it.faktum().let { faktum ->
                faktum.faktumId to faktum
            }
        }.toMap().toMutableMap().also { faktumMap ->
            factories.forEach { factory ->
                factory.tilTemplate(faktumMap)
            }
            factories.forEach { factory ->
                factory.avhengerAv(faktumMap)
                factory.sammensattAv(faktumMap)
            }
        }
    )

    override infix fun id(rootId: Int) = id(FaktumId(rootId))
    override infix fun id(id: String) = id(FaktumId(id))
    internal infix fun id(faktumId: FaktumId) = faktumMap[faktumId] ?: throw IllegalArgumentException("Ukjent id $faktumId")
    internal infix fun idOrNull(faktumId: FaktumId) = faktumMap[faktumId]

    override infix fun dokument(rootId: Int) = dokument(FaktumId(rootId))
    override infix fun dokument(id: String) = dokument(FaktumId(id))
    internal infix fun dokument(faktumId: FaktumId) = id(faktumId) as Faktum<Dokument>

    override fun inntekt(rootId: Int) = inntekt(FaktumId(rootId))
    override fun inntekt(id: String) = inntekt(FaktumId(id))
    internal infix fun inntekt(faktumId: FaktumId) = id(faktumId) as Faktum<Inntekt>

    override infix fun ja(rootId: Int) = ja(FaktumId(rootId))
    override infix fun ja(id: String) = ja(FaktumId(id))
    internal infix fun ja(faktumId: FaktumId) = id(faktumId) as Faktum<Boolean>

    override infix fun dato(rootId: Int) = dato(FaktumId(rootId))
    override infix fun dato(id: String) = dato(FaktumId(id))
    internal infix fun dato(faktumId: FaktumId) = id(faktumId) as Faktum<LocalDate>

    override infix fun heltall(rootId: Int) = heltall(FaktumId(rootId))
    override infix fun heltall(id: String) = heltall(FaktumId(id))
    internal infix fun heltall(faktumId: FaktumId) = id(faktumId) as Faktum<Int>

    override infix fun generator(rootId: Int) = generator(FaktumId(rootId))
    override infix fun generator(id: String) = heltall(FaktumId(id))
    internal infix fun generator(faktumId: FaktumId) = id(faktumId) as GeneratorFaktum

    fun bygg(fnr: String): Fakta {
        val byggetFakta = mutableMapOf<FaktumId, Faktum<*>>()
        val mapOfFakta = faktumMap.map { it.key to it.value.bygg(byggetFakta) }.toMap().toMutableMap()
        return Fakta(fnr, mapOfFakta)
    }

    override fun iterator(): MutableIterator<Faktum<*>> {
        return faktumMap.values.sorted().sortUtledet().iterator()
    }

    private fun List<Faktum<*>>.sortUtledet(): MutableList<Faktum<*>> {
        this.forEachIndexed { indeks, faktum ->
            if (faktum !is UtledetFaktum) return@forEachIndexed
            if (faktum.erDefinert(this, indeks)) return@forEachIndexed
            val nyListe = this.toMutableList().also {
                it.add(it.removeAt(indeks))
            }
            return@sortUtledet nyListe.sortUtledet()
        }
        return this.toMutableList()
    }
}
