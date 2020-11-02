package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.factory.FaktaRegel.Companion.ALLE_JA
import no.nav.dagpenger.model.factory.FaktaRegel.Companion.EN_ELLER_INGEN
import no.nav.dagpenger.model.factory.FaktaRegel.Companion.MAKS_DATO
import no.nav.dagpenger.model.factory.FaktaRegel.Companion.MAKS_INNTEKT
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
    private val childJaNavn = mutableSetOf<String>()
    private val childNeiNavn = mutableSetOf<String>()

    companion object {
        object maks {
            infix fun dato(navn: String) = UtledetFaktumFactory(navn, MAKS_DATO)
            infix fun inntekt(navn: String) = UtledetFaktumFactory(navn, MAKS_INNTEKT)
        }

        object alle {
            infix fun ja(navn: String) = UtledetFaktumFactory(navn, ALLE_JA)
        }

        object valg {
            infix fun faktum(navn: String) = UtledetFaktumFactory(navn, EN_ELLER_INGEN)
        }

        private fun List<FaktumFactory<*>>.maksIndeks() = this.maxOf { it.rootId }
    }

    infix fun ja(navn: String) = this.also { childJaNavn.add(navn) }

    infix fun nei(navn: String) = this.also { childNeiNavn.add(navn) }

    override fun og(otherId: Int) = this.also { childIder.add(otherId) }

    infix fun av(otherId: Int) = this.also { childIder.add(otherId) }

    infix fun id(rootId: Int) = this.also { this.rootId = rootId }

    override fun faktum() = UtledetFaktum(faktumId, navn, fakta, regel)

    internal fun ekspanderValg(factories: MutableList<FaktumFactory<*>>) {
        val førsteIndeks = factories.maksIndeks() + 1
        val maksIndeks = førsteIndeks + childJaNavn.size + childNeiNavn.size - 1
        var nesteIndeks = førsteIndeks

        (childJaNavn + childNeiNavn).map { navn ->
            (BaseFaktumFactory.Companion.ja nei navn id nesteIndeks).also { factory ->
                (førsteIndeks..maksIndeks).forEach {
                    if (it != nesteIndeks) factory avhengerAv it
                }
                factories.add(factory)
                childIder.add(nesteIndeks)
                nesteIndeks++
            }
        }
    }

    override fun sammensattAv(faktumMap: Map<FaktumId, Faktum<*>>) {
        (faktumMap[FaktumId(rootId)] as UtledetFaktum).addAll(
            childIder.map { otherId ->
                faktumMap[FaktumId(otherId)] as Faktum
            }
        )
    }

    private val faktumId get() = FaktumId(rootId).also { require(rootId != 0) }
}

class FaktaRegel<R : Comparable<R>> private constructor(val navn: String, internal val strategy: (UtledetFaktum<R>) -> R) {

    companion object {
        internal val MAKS_DATO = FaktaRegel("MAKS_DATO", UtledetFaktum<LocalDate>::max)
        internal val MAKS_INNTEKT = FaktaRegel("MAKS_INNTEKT", UtledetFaktum<Inntekt>::max)
        internal val ALLE_JA = FaktaRegel("ALLE_JA", UtledetFaktum<Boolean>::alle)
        internal val EN_ELLER_INGEN = FaktaRegel("EN_ELLER_INGEN", UtledetFaktum<Boolean>::valg)
    }
}
