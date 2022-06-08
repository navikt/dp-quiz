package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Rolle

abstract class FaktumFactory<T : Comparable<T>> {
    internal var rootId = 0
    private val avhengigheter = mutableListOf<Int>()
    protected val roller = mutableSetOf<Rolle>()

    internal abstract fun faktum(): Faktum<T>

    infix fun avhengerAv(otherId: Int) = this.also { avhengigheter.add(otherId) }

    internal fun avhengerAv(faktumMap: Map<FaktumId, Faktum<*>>) {
        avhengigheter.forEach {
            (faktumMap[FaktumId(it)] as Faktum<*>).harAvhengighet(faktumMap[FaktumId(rootId)] as Faktum<*>)
        }
    }

    open infix fun og(otherId: Int) = avhengerAv(otherId)

    open infix fun genererer(otherId: Int): BaseFaktumFactory<Int> = this as BaseFaktumFactory<Int>

    internal open fun sammensattAv(faktumMap: Map<FaktumId, Faktum<*>>) {}

    internal open fun tilTemplate(faktumMap: MutableMap<FaktumId, Faktum<*>>) {}
    infix fun kanEndresAv(rolle: Rolle): FaktumFactory<T> = this.also { roller.add(rolle) }
}
