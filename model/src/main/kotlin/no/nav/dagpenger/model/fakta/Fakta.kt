package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.factory.FaktumFactory
import java.time.LocalDate
import java.util.UUID

class Fakta private constructor(
    fnr: String,
    uuid: UUID,
    private val faktumMap: MutableMap<FaktumId, Faktum<*>>
) : MutableList<Faktum<*>> by faktumMap.values.toMutableList() {

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

    infix fun id(rootId: Int) = id(FaktumId(rootId))

    infix fun id(id: String) = id(FaktumId(id))

    internal infix fun id(faktumId: FaktumId) = faktumMap[faktumId] ?: throw IllegalArgumentException("Ukjent id $faktumId")

    infix fun dokument(rootId: Int) = dokument(FaktumId(rootId))

    internal infix fun dokument(faktumId: FaktumId) = id(faktumId) as Faktum<Dokument>

    infix fun inntekt(rootId: Int) = inntekt(FaktumId(rootId))

    internal infix fun inntekt(faktumId: FaktumId) = id(faktumId) as Faktum<Inntekt>

    infix fun ja(rootId: Int) = ja(FaktumId(rootId))

    internal infix fun ja(faktumId: FaktumId) = id(faktumId) as Faktum<Boolean>

    infix fun dato(rootId: Int) = dato(FaktumId(rootId))

    internal infix fun dato(faktumId: FaktumId) = id(faktumId) as Faktum<LocalDate>

    infix fun heltall(rootId: Int) = heltall(FaktumId(rootId))

    infix fun generator(rootId: Int) = generator(FaktumId(rootId))

    internal infix fun generator(faktumId: FaktumId) = id(faktumId) as GeneratorFaktum

    internal infix fun heltall(faktumId: FaktumId) = id(faktumId) as Faktum<Int>

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
