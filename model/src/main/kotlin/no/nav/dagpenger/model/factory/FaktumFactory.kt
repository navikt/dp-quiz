package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId

abstract class FaktumFactory<T : Comparable<T>> {
    internal var rootId = 0
    private val avhengigheter = mutableListOf<Int>()

    internal abstract fun faktum(): Faktum<T>

    infix fun avhengerAv(otherId: Int) = this.also { avhengigheter.add(otherId) }

    internal fun avhengerAv(faktumMap: Map<FaktumId, Faktum<*>>) {
        avhengigheter.forEach {
            faktumMap[FaktumId(rootId)]?.avhengerAv(faktumMap[FaktumId(it)] as Faktum<*>)
        }
    }

    open infix fun og(otherId: Int) = avhengerAv(otherId)

    open infix fun genererer(otherId: Int): BaseFaktumFactory<Int> = this as BaseFaktumFactory<Int>

    internal open fun sammensattAv(faktumMap: Map<FaktumId, Faktum<*>>) {}

    internal open fun valgMellom(faktumMap: Map<FaktumId, Faktum<*>>) {}

    internal open fun tilTemplate(faktumMap: MutableMap<FaktumId, Faktum<*>>) {}
}
