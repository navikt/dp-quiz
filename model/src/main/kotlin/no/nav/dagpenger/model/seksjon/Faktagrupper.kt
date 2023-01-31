package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TypedFaktum
import no.nav.dagpenger.model.seksjon.Seksjon.Companion.saksbehandlerSeksjoner
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor

class Faktagrupper private constructor(
    val søknad: Søknad,
    internal val rootSubsumsjon: Subsumsjon,
    private val seksjoner: MutableList<Seksjon>
) : TypedFaktum by søknad, MutableList<Seksjon> by seksjoner {
    constructor(vararg seksjoner: Seksjon) : this(
        Søknad(Prosessversjon.prototypeversjon),
        TomSubsumsjon,
        seksjoner.toMutableList()
    )

    internal constructor(søknad: Søknad, vararg seksjoner: Seksjon, rootSubsumsjon: Subsumsjon = TomSubsumsjon) : this(
        søknad,
        rootSubsumsjon,
        seksjoner.toMutableList()
    )

    init {
        seksjoner.forEach {
            it.søknadprosess(this)
        }
    }

    internal fun add(faktum: Faktum<*>) = søknad.add(faktum)

    internal infix fun idOrNull(faktumId: FaktumId) = søknad.idOrNull(faktumId)

    @Suppress("UNCHECKED_CAST")
    fun <T : Comparable<T>> faktum(id: String): Faktum<T> = (søknad.id(id) as Faktum<T>)

    @Suppress("UNCHECKED_CAST")
    fun <T : Comparable<T>> faktum(id: Int): Faktum<T> = (søknad.id(id) as Faktum<T>)

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
        søknad.accept(visitor)
        seksjoner.forEach { it.accept(visitor) }
        rootSubsumsjon.accept(visitor)
        visitor.postVisit(this)
    }

    internal fun faktum(id: FaktumId) = søknad.id(id)

    fun seksjon(navn: String) = seksjoner.first { it.navn == navn }

    internal fun bygg(søknad: Søknad, subsumsjon: Subsumsjon) =
        Faktagrupper(søknad, subsumsjon, seksjoner.map { it.bygg(søknad) }.toMutableList())

    internal fun nesteFakta() = rootSubsumsjon.nesteFakta()

    fun resultat() = rootSubsumsjon.resultat()

    fun erFerdig() = nesteSeksjoner().all { fakta -> fakta.all { faktum -> faktum.erBesvart() } }
    fun erFerdigFor(vararg roller: Rolle): Boolean = nesteSeksjoner().all { fakta -> fakta.none { faktum -> roller.any { faktum.harRolle(it) } } }
}
