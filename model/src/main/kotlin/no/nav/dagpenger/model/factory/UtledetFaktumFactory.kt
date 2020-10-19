package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.fakta.FaktaRegel
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.regel.MAKS_DATO

class UtledetFaktumFactory<T : Comparable<T>>(
    private val navn: String,
    private val regel: FaktaRegel<T>
): FaktumFactory<T> {
    private var rootId = 0
    private val fakta = mutableSetOf<Faktum<T>>()

    companion object {
        object maks { infix fun dato(navn: String) = UtledetFaktumFactory(navn, MAKS_DATO) }
    }

    infix fun av(factum: Faktum<T>) = this.also { fakta.add(factum) }

    //infix fun av(factory: BaseFaktumFactory<T>) = this.also { fakta.add(factory.faktum) }

    infix fun og(factum: Faktum<T>) = this.also { fakta.add(factum) }

    //infix fun og(factory: BaseFaktumFactory<T>) = this.also { fakta.add(factory.faktum) }

    infix fun id(rootId: Int) = this.also { this.rootId = rootId }

    override val faktum get() = UtledetFaktum<T>(faktumNavn, fakta, regel)

    private val faktumNavn get() = FaktumNavn(rootId, navn).also { require(rootId != 0) }
}
