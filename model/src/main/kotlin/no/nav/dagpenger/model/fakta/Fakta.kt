package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.factory.FaktumFactory
import java.time.LocalDate
import java.util.UUID

class Fakta private constructor(fnr: String, uuid: UUID, versjon: Versjon, private val faktumMap: Map<FaktumId, Faktum<*>>) {

    constructor(fnr: String, vararg factories: FaktumFactory<*>) : this(
        fnr,
        UUID.randomUUID(),
        Versjon.V1,
        factories.map {
            it.faktum().let { faktum ->
                faktum.faktumId to faktum
            }
        }.toMap().also { faktumMap ->
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

    private enum class Versjon { V1 }
}
