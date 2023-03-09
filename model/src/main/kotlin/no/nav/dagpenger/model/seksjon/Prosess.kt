package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TypedFaktum
import no.nav.dagpenger.model.seksjon.Seksjon.Companion.saksbehandlerSeksjoner
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.ProsessVisitor
import java.util.UUID

interface Prosesstype {
    val navn: String
    val faktatype: Faktatype
}

class Prosess private constructor(
    val uuid: UUID,
    val type: Prosesstype,
    val fakta: Fakta,
    internal val rootSubsumsjon: Subsumsjon,
    private val seksjoner: MutableList<Seksjon>,
) : TypedFaktum by fakta, MutableList<Seksjon> by seksjoner {
    constructor(type: Prosesstype, vararg seksjoner: Seksjon) : this(
        UUID.randomUUID(),
        type,
        Fakta(Faktaversjon.prototypeversjon),
        TomSubsumsjon,
        seksjoner.toMutableList(),
    )

    internal constructor(
        type: Prosesstype,
        fakta: Fakta,
        vararg seksjoner: Seksjon,
        rootSubsumsjon: Subsumsjon = TomSubsumsjon,
    ) : this(
        UUID.randomUUID(),
        type,
        fakta,
        rootSubsumsjon,
        seksjoner.toMutableList(),
    )

    init {
        // TODO: require(type.faktatype.id == fakta.faktaversjon.faktatype.id) { "Kan ikke bruke denne type fakta for denne prosessen" }
        seksjoner.forEach {
            it.søknadprosess(this)
        }
    }

    internal fun add(faktum: Faktum<*>) = fakta.add(faktum)

    internal infix fun idOrNull(faktumId: FaktumId) = fakta.idOrNull(faktumId)

    @Suppress("UNCHECKED_CAST")
    fun <T : Comparable<T>> faktum(id: String): Faktum<T> = (fakta.id(id) as Faktum<T>)

    @Suppress("UNCHECKED_CAST")
    fun <T : Comparable<T>> faktum(id: Int): Faktum<T> = (fakta.id(id) as Faktum<T>)

    fun nesteSeksjoner(): List<Seksjon> =
        if (rootSubsumsjon.resultat() != null) {
            saksbehandlerSeksjoner(rootSubsumsjon.relevanteFakta())
        } else {
            val nesteFakta = rootSubsumsjon.nesteFakta()
            listOf(
                seksjoner.firstOrNull { nesteFakta in it } ?: throw NoSuchElementException(
                    "Fant ikke seksjon med fakta:\n ${
                    nesteFakta.map { "Id=${it.id}, navn='${it.navn}'" }
                    }",
                ),
            )
        }

    fun accept(visitor: ProsessVisitor) {
        visitor.preVisit(this, uuid)
        fakta.accept(visitor)
        seksjoner.forEach { it.accept(visitor) }
        rootSubsumsjon.accept(visitor)
        visitor.postVisit(this, uuid)
    }

    internal fun faktum(id: FaktumId) = fakta.id(id)

    fun seksjon(navn: String) = seksjoner.first { it.navn == navn }

    internal fun bygg(prosessUUID: UUID, fakta: Fakta, subsumsjon: Subsumsjon) =
        Prosess(prosessUUID, type, fakta, subsumsjon, seksjoner.map { it.bygg(fakta) }.toMutableList())

    // Brukes for å bygge en prosess uten person, for publisering av maler
    fun bygg(fakta: Fakta, subsumsjon: Subsumsjon) =
        Prosess(UUID.randomUUID(), type, fakta, subsumsjon, seksjoner.map { it.bygg(fakta) }.toMutableList())

    internal fun nesteFakta() = rootSubsumsjon.nesteFakta()

    fun resultat() = rootSubsumsjon.resultat()

    fun erFerdig() = nesteSeksjoner().all { fakta -> fakta.all { faktum -> faktum.erBesvart() } }
    fun erFerdigFor(vararg roller: Rolle): Boolean =
        nesteSeksjoner().all { fakta -> fakta.none { faktum -> roller.any { faktum.harRolle(it) } } }
}
