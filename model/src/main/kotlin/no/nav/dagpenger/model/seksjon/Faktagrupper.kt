package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.TypedFaktum
import no.nav.dagpenger.model.seksjon.Seksjon.Companion.saksbehandlerSeksjoner
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor

class Faktagrupper private constructor(
    val fakta: Fakta,
    internal val rootSubsumsjon: Subsumsjon,
    private val seksjoner: MutableList<Seksjon>
) : TypedFaktum by fakta, MutableList<Seksjon> by seksjoner {
    constructor(vararg seksjoner: Seksjon) : this(
        Fakta(Prosessversjon.prototypeversjon),
        TomSubsumsjon,
        seksjoner.toMutableList()
    )

    internal constructor(fakta: Fakta, vararg seksjoner: Seksjon, rootSubsumsjon: Subsumsjon = TomSubsumsjon) : this(
        fakta,
        rootSubsumsjon,
        seksjoner.toMutableList()
    )

    init {
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
                    }"
                )
            )
        }

    fun accept(visitor: SøknadprosessVisitor) {
        visitor.preVisit(this)
        fakta.accept(visitor)
        seksjoner.forEach { it.accept(visitor) }
        rootSubsumsjon.accept(visitor)
        visitor.postVisit(this)
    }

    internal fun faktum(id: FaktumId) = fakta.id(id)

    fun seksjon(navn: String) = seksjoner.first { it.navn == navn }

    internal fun bygg(fakta: Fakta, subsumsjon: Subsumsjon) =
        Faktagrupper(fakta, subsumsjon, seksjoner.map { it.bygg(fakta) }.toMutableList())

    internal fun nesteFakta() = rootSubsumsjon.nesteFakta()

    fun resultat() = rootSubsumsjon.resultat()

    fun erFerdig() = nesteSeksjoner().all { fakta -> fakta.all { faktum -> faktum.erBesvart() } }
    fun erFerdigFor(vararg roller: Rolle): Boolean = nesteSeksjoner().all { fakta -> fakta.none { faktum -> roller.any { faktum.harRolle(it) } } }
}
