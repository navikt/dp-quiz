package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.ValgFaktum

class ValgFaktumFactory(
    private val navn: String,
) : FaktumFactory<Boolean>() {
    private val jaIder = mutableSetOf<Int>()
    private val neiIder = mutableSetOf<Int>()
    private val underordnedeJaNavn = mutableSetOf<String>()
    private val underordnedeNeiNavn = mutableSetOf<String>()

    companion object {
        object valg {
            infix fun faktum(navn: String) = ValgFaktumFactory(navn)
        }

        private fun List<FaktumFactory<*>>.maksIndeks() = this.maxOf { it.rootId }
    }

    infix fun ja(navn: String) = this.also { underordnedeJaNavn.add(navn) }

    infix fun nei(navn: String) = this.also { underordnedeNeiNavn.add(navn) }

    infix fun id(rootId: Int) = this.also { this.rootId = rootId }

    override fun faktum() = ValgFaktum(faktumId, navn, mutableSetOf(), mutableSetOf())

    internal fun ekspanderValg(factories: MutableList<FaktumFactory<*>>) {
        val førsteIndeks = factories.maksIndeks() + 1
        val maksIndeks = førsteIndeks + underordnedeJaNavn.size + underordnedeNeiNavn.size - 1

        val antallFaktum = førsteIndeks..maksIndeks

        underordnedeJaNavn.opprettUnderordnedeFactories(factories, førsteIndeks, antallFaktum)
            .also { jaIder.addAll(it) }
        underordnedeNeiNavn.opprettUnderordnedeFactories(factories, førsteIndeks + underordnedeJaNavn.size, antallFaktum)
            .also { neiIder.addAll(it) }
    }

    private fun MutableSet<String>.opprettUnderordnedeFactories(
        factories: MutableList<FaktumFactory<*>>,
        førsteIndeks: Int,
        faktumIndeks: IntRange
    ): List<Int> {
        var nesteIndeks = førsteIndeks
        return this.map { navn ->
            (BaseFaktumFactory.Companion.ja nei navn id nesteIndeks).also { factory ->
                faktumIndeks.forEach {
                    if (it != nesteIndeks) factory avhengerAv it
                }
                factories.add(factory)
            }
            nesteIndeks++
        }
    }

    override fun valgMellom(faktumMap: Map<FaktumId, Faktum<*>>) {
        (faktumMap[FaktumId(rootId)] as ValgFaktum).addAllJa(
            jaIder.map { otherId ->
                faktumMap[FaktumId(otherId)] as Faktum
            }
        )
        (faktumMap[FaktumId(rootId)] as ValgFaktum).addAllNei(
            neiIder.map { otherId ->
                faktumMap[FaktumId(otherId)] as Faktum
            }
        )
    }

    private val faktumId get() = FaktumId(rootId).also { require(rootId != 0) }
}
