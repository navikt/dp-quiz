package no.nav.dagpenger.model.seksjon

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TypedFaktum
import no.nav.dagpenger.model.seksjon.Seksjon.Companion.saksbehandlerSeksjoner
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadprosessVisitor
import java.util.UUID

class Søknadprosess private constructor(
    val søknad: Søknad,
    internal val rootSubsumsjon: Subsumsjon,
    private val uuid: UUID,
    private val seksjoner: MutableList<Seksjon>
) : TypedFaktum by søknad, MutableList<Seksjon> by seksjoner {

    constructor(vararg seksjoner: Seksjon) : this(
        Søknad(),
        TomSubsumsjon,
        UUID.randomUUID(),
        seksjoner.toMutableList()
    )

    internal constructor(søknad: Søknad, vararg seksjoner: Seksjon, rootSubsumsjon: Subsumsjon = TomSubsumsjon) : this(
        søknad,
        rootSubsumsjon,
        UUID.randomUUID(),
        seksjoner.toMutableList()
    )

    init {
        seksjoner.forEach {
            it.søknadprosess(this)
        }
    }

    internal fun add(faktum: Faktum<*>) = søknad.add(faktum)

    internal infix fun idOrNull(faktumId: FaktumId) = søknad.idOrNull(faktumId)

    fun <T : Comparable<T>> faktum(id: String): Faktum<T> = (søknad.id(id) as Faktum<T>)

    fun <T : Comparable<T>> faktum(id: Int): Faktum<T> = (søknad.id(id) as Faktum<T>)

    fun nesteSeksjoner(): List<Seksjon> =
        if (rootSubsumsjon.resultat() != null)
            saksbehandlerSeksjoner(rootSubsumsjon.relevanteFakta())
        else
            listOf(seksjoner.first { rootSubsumsjon.nesteFakta() in it })

    fun accept(visitor: SøknadprosessVisitor) {
        visitor.preVisit(this, uuid)
        søknad.accept(visitor)
        seksjoner.forEach { it.accept(visitor) }
        rootSubsumsjon.accept(visitor)
        visitor.postVisit(this)
    }

    internal fun faktum(id: FaktumId) = søknad.id(id)

    fun seksjon(navn: String) = seksjoner.first { it.navn == navn }

    internal fun bygg(søknad: Søknad, subsumsjon: Subsumsjon) =
        Søknadprosess(søknad, subsumsjon, UUID.randomUUID(), seksjoner.map { it.bygg(søknad) }.toMutableList())

    internal fun nesteFakta() = rootSubsumsjon.nesteFakta()

    fun resultat() = rootSubsumsjon.resultat()
}
