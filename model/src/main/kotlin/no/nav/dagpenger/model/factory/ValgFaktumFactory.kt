package no.nav.dagpenger.model.factory

import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.ValgFaktum

class ValgFaktumFactory(
    private val navn: String,
) : FaktumFactory<Boolean>() {
    private val jaIder = mutableSetOf<Int>()
    private val neiIder = mutableSetOf<Int>()
    private val childJaNavn = mutableSetOf<String>()
    private val childNeiNavn = mutableSetOf<String>()

    companion object {
        object valg {
            infix fun faktum(navn: String) = ValgFaktumFactory(navn)
        }

        private fun List<FaktumFactory<*>>.maksIndeks() = this.maxOf { it.rootId }
    }

    infix fun ja(navn: String) = this.also { childJaNavn.add(navn) }

    infix fun nei(navn: String) = this.also { childNeiNavn.add(navn) }

    infix fun id(rootId: Int) = this.also { this.rootId = rootId }

    override fun faktum() = ValgFaktum(faktumId, navn, mutableSetOf(), mutableSetOf())

    internal fun ekspanderValg(factories: MutableList<FaktumFactory<*>>) {
        val førsteIndeks = factories.maksIndeks() + 1
        val maksIndeks = førsteIndeks + childJaNavn.size + childNeiNavn.size - 1
        var nesteIndeks = førsteIndeks

        childJaNavn.map { navn ->
            (BaseFaktumFactory.Companion.ja nei navn id nesteIndeks).also {
                jaIder.add(nesteIndeks)
            }.also { factory ->
                (førsteIndeks..maksIndeks).forEach {
                    if (it != nesteIndeks) factory avhengerAv it
                }
                factories.add(factory)
                nesteIndeks++
            }
        }
        childNeiNavn.map { navn ->
            (BaseFaktumFactory.Companion.ja nei navn id nesteIndeks).also {
                neiIder.add(nesteIndeks)
            }.also { factory ->
                (førsteIndeks..maksIndeks).forEach {
                    if (it != nesteIndeks) factory avhengerAv it
                }
                factories.add(factory)
                nesteIndeks++
            }
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
