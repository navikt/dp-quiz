package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.søknad.Søknad
import no.nav.dagpenger.model.visitor.FaktumVisitor

class TemplateFaktum<R : Comparable<R>> internal constructor(faktumId: FaktumId, navn: String, internal val clazz: Class<R>) : Faktum<R>(faktumId, navn) {
    private val seksjoner = mutableListOf<Seksjon>()

    override fun clazz() = clazz

    override fun tilUbesvart() {
        // Ignorert
    }

    override fun svar(): R {
        throw IllegalStateException("Templates har ikke svar")
    }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        // Ignorert
    }

    override fun erBesvart() = false

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        visitor.visit(this, id, avhengigeFakta, roller, clazz)
    }

    override fun add(seksjon: Seksjon) = seksjoner.add(seksjon)

    override fun med(indeks: Int, søknad: Søknad): Faktum<*> {
        return søknad.fakta[faktumId.indeks(indeks)]
            ?: GrunnleggendeFaktum(
                faktumId.indeks(indeks),
                navn,
                clazz,
                avhengigeFakta.deepCopy(indeks, søknad).toMutableSet(),
                roller
            ).also { søknad.fakta[it.faktumId] = it }
    }

    internal fun generate(r: Int) {
        seksjoner.forEach { originalSeksjon ->
            (1..r).forEach { indeks ->
                val seksjon = if (originalSeksjon.bareTemplates()) {
                    originalSeksjon.deepCopy(indeks)
                } else {
                    originalSeksjon
                }
                seksjon.add(
                    GrunnleggendeFaktum(
                        faktumId.indeks(indeks),
                        navn,
                        clazz,
                        avhengigeFakta.deepCopy(indeks, seksjon.søknad).toMutableSet(),
                        roller
                    )
                )
            }
        }
    }
}
