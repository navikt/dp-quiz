package no.nav.dagpenger.model.fakta

import no.nav.dagpenger.model.søknad.Seksjon
import no.nav.dagpenger.model.visitor.FaktumVisitor

class TemplateFaktum<R : Comparable<R>> internal constructor(
    faktumId: FaktumId,
    navn: String,
    internal val clazz: Class<R>,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf()
) : Faktum<R>(faktumId, navn, avhengigeFakta, roller) {
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

    override fun deepCopy(indeks: Int, fakta: Fakta): Faktum<*> {
        return fakta.idOrNull(faktumId medIndeks indeks )
            ?: GrunnleggendeFaktum(
                faktumId medIndeks indeks,
                navn,
                clazz,
                avhengigeFakta.deepCopy(indeks, fakta).toMutableSet(),
                roller
            ).also { fakta.add(it) }
    }

    internal fun generate(r: Int, fakta: Fakta) {
        seksjoner.forEach { originalSeksjon ->
            (1..r).forEach { indeks ->
                val seksjon = if (originalSeksjon.bareTemplates()) {
                    originalSeksjon.deepCopy(indeks, fakta)
                } else {
                    originalSeksjon
                }
                seksjon.add(
                    GrunnleggendeFaktum(
                        faktumId.medIndeks(indeks),
                        navn,
                        clazz,
                        avhengigeFakta.deepCopy(indeks, fakta).toMutableSet(),
                        roller
                    )
                )
            }
        }
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*> {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId]!!
        val avhengigheter = avhengigeFakta.map { it.bygg(byggetFakta) }.toMutableSet()
        return TemplateFaktum(faktumId, navn, clazz, avhengigheter, roller).also { byggetFakta[faktumId] = it }
    }
}
