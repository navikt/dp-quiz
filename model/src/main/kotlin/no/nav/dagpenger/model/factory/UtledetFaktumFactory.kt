package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.fakta.FaktaRegel
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.regel.ALLE_JA
import no.nav.dagpenger.model.regel.MAKS_DATO
import no.nav.dagpenger.model.regel.MAKS_INNTEKT

class UtledetFaktumFactory<T : Comparable<T>>(
    private val navn: String,
    private val regel: FaktaRegel<T>
) : FaktumFactory<T>() {
    private val fakta = mutableSetOf<Faktum<T>>()
    private val childIder = mutableSetOf<Int>()

    companion object {
        object maks {
            infix fun dato(navn: String) = UtledetFaktumFactory(navn, MAKS_DATO)
            infix fun inntekt(navn: String) = UtledetFaktumFactory(navn, MAKS_INNTEKT)
        }

        object alle {
            infix fun ja(navn: String) = UtledetFaktumFactory(navn, ALLE_JA)
        }
    }

    infix fun av(factum: Faktum<T>) = this.also { fakta.add(factum) }

    infix fun og(factum: Faktum<T>) = this.also { fakta.add(factum) }

    override fun og(otherId: Int) = this.also { childIder.add(otherId) }

    infix fun av(otherId: Int) = this.also { childIder.add(otherId) }

    infix fun id(rootId: Int) = this.also { this.rootId = rootId }

    override fun faktum() = UtledetFaktum(faktumId, navn, fakta, regel)

    override fun sammensattAv(faktumMap: Map<FaktumId, Faktum<*>>) {
        (faktumMap[FaktumId(rootId)] as UtledetFaktum).addAll(
            childIder.map { otherId ->
                faktumMap[FaktumId(otherId)] as Faktum
            }
        )
    }

    private val faktumId get() = FaktumId(rootId).also { require(rootId != 0) }
}
