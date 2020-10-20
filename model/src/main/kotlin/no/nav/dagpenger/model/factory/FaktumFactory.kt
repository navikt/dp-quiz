package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId

abstract class FaktumFactory <T : Comparable<T>> {
    protected var rootId = 0
    private val avhengigheter = mutableListOf<Int>()

    internal abstract fun faktum(): Faktum<T>

    infix fun avhengerAv(otherId: Int) = this.also { avhengigheter.add(otherId) }

    internal fun avhengerAv(faktumMap: Map<FaktumId, Faktum<*>>) {
        avhengigheter.forEach {
            faktumMap[FaktumId(rootId)]?.avhengerAv(faktumMap[FaktumId(it)] as Faktum<*>)
        }
    }

    infix fun og(otherId: Int) = avhengerAv(otherId)
}
