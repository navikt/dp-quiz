package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.regel.MAKS_DATO

class UtledetFaktumFactory<T : Comparable<T>>(
    private val navn: String,
    private val regel: FaktaRegel<T>
) {
    private var rootId = 0
    private val fakta = mutableSetOf<Faktum<T>>()

    companion object {
        object maks { infix fun dato(navn: String) = UtledetFaktumFactory(navn, MAKS_DATO) }
    }

    infix fun av(factum: Faktum<T>) = this.also { fakta.add(factum) }

    infix fun av(factory: FaktumFactory<T>) = this.also { fakta.add(factory.faktum) }

    infix fun og(factum: Faktum<T>) = this.also { fakta.add(factum) }

    infix fun og(factory: FaktumFactory<T>) = this.also { fakta.add(factory.faktum) }

    infix fun id(rootId: Int) = this.also { this.rootId = rootId }

    val faktum get() = UtledetFaktum<T>(faktumNavn, fakta, regel)

    private val faktumNavn get() = FaktumNavn(rootId, navn).also { require(rootId != 0) }
}
