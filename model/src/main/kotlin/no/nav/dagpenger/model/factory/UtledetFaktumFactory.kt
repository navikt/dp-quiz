package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.factory.FaktaRegel.Companion.ALLE_JA
import no.nav.dagpenger.model.factory.FaktaRegel.Companion.MAKS_DATO
import no.nav.dagpenger.model.factory.FaktaRegel.Companion.MAKS_INNTEKT
import no.nav.dagpenger.model.factory.FaktaRegel.Companion.MIN_DATO
import no.nav.dagpenger.model.factory.FaktaRegel.Companion.MULTIPLIKASJON_INNTEKT
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.UtledetFaktum
import java.time.LocalDate

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

        object min {
            infix fun dato(navn: String) = UtledetFaktumFactory(navn, MIN_DATO)
        }

        object multiplikasjon {
            infix fun inntekt(navn: String) = UtledetFaktumFactory(navn, MULTIPLIKASJON_INNTEKT)
        }

        object alle {
            infix fun ja(navn: String) = UtledetFaktumFactory(navn, ALLE_JA)
        }
    }

    override fun og(otherId: Int) = this.also { childIder.add(otherId) }

    infix fun av(otherId: Int) = this.also { childIder.add(otherId) }

    infix fun ganger(otherId: Int) = this.also { childIder.add(otherId) }

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

class FaktaRegel<R : Comparable<R>> private constructor(
    val navn: String,
    internal val strategy: (UtledetFaktum<R>) -> R
) {

    companion object {
        internal val MAKS_DATO = FaktaRegel("MAKS_DATO", UtledetFaktum<LocalDate>::max)
        internal val MIN_DATO = FaktaRegel("MIN_DATO", UtledetFaktum<LocalDate>::min)
        internal val MAKS_INNTEKT = FaktaRegel("MAKS_INNTEKT", UtledetFaktum<Inntekt>::max)
        internal val MULTIPLIKASJON_INNTEKT =
            FaktaRegel("MULTIPLIKASJON_INNTEKT", UtledetFaktum<Inntekt>::multiplikasjon)
        internal val ALLE_JA = FaktaRegel("ALLE_JA", UtledetFaktum<Boolean>::alle)
    }
}
