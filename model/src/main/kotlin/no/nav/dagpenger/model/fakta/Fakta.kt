package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.factory.FaktumFactory
import java.time.LocalDate
import java.util.UUID

class Fakta private constructor(fnr: String, uuid: UUID, private val faktumMap: MutableMap<FaktumId, Faktum<*>>) {

    constructor(fnr: String, faktumMap: MutableMap<FaktumId, Faktum<*>>) : this(fnr, UUID.randomUUID(), faktumMap)

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

    infix fun id(rootId: Int) = id(rootId.toString())

    infix fun dokument(rootId: Int) = id(rootId.toString()) as Faktum<Dokument>

    infix fun inntekt(rootId: Int) = id(rootId.toString()) as Faktum<Inntekt>

    infix fun ja(rootId: Int) = id(rootId.toString()) as Faktum<Boolean>

    infix fun dato(rootId: Int) = id(rootId.toString()) as Faktum<LocalDate>

    infix fun id(id: String) =
        faktumMap[FaktumId(id)] ?: throw IllegalArgumentException("Ukjent id $id")

    infix fun heltall(rootId: Int) = id(rootId.toString()) as Faktum<Int>

    fun bygg(fnr: String): Fakta {
        val byggetFakta = mutableMapOf<FaktumId, Faktum<*>>()
        val mapOfFakta = faktumMap.map { it.key to it.value.bygg(byggetFakta) }.toMap().toMutableMap()
        return Fakta(fnr, mapOfFakta)
    }
}
