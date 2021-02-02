package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.visitor.FaktumVisitor

class TemplateFaktum<R : Comparable<R>> internal constructor(
    faktumId: FaktumId,
    navn: String,
    internal val clazz: Class<R>,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf()
) : Faktum<R>(faktumId, navn, avhengigeFakta, avhengerAvFakta, roller) {
    private val seksjoner = mutableListOf<Seksjon>()

    override fun clazz() = clazz

    override fun tilUbesvart() {
        // Ignorert
    }

    override fun svar(): R {
        throw IllegalStateException("Templates har ikke svar")
    }

    override fun besvartAv(): String? {
        throw IllegalStateException("Templates har ikke svar")    }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

    override fun leggTilHvis(kode: Faktum.FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        // Ignorert
    }

    override fun erBesvart() = false

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        visitor.visit(this, id, avhengigeFakta, avhengerAvFakta, roller, clazz)
    }

    override fun add(seksjon: Seksjon) =
        seksjoner.add(seksjon)

    override fun deepCopy(indeks: Int, søknad: Søknad): Faktum<*> {
        return søknad.idOrNull(faktumId medIndeks indeks)
            ?: GrunnleggendeFaktum(
                faktumId medIndeks indeks,
                navn,
                clazz,
                avhengigeFakta.toList().deepCopy(indeks, søknad).toMutableSet(),
                avhengerAvFakta.toList().deepCopy(indeks, søknad).toMutableSet(),
                mutableSetOf(),
                roller
            ).also { søknad.add(it) }
    }

    internal fun generate(r: Int, søknad: Søknad) {
        seksjoner.forEach { originalSeksjon ->
            (1..r).forEach { indeks ->
                val seksjon = if (originalSeksjon.bareTemplates()) {
                    originalSeksjon.deepCopy(indeks, søknad)
                } else {
                    originalSeksjon
                }
                seksjon.add(
                    GrunnleggendeFaktum(
                        faktumId.medIndeks(indeks),
                        navn,
                        clazz,
                        avhengigeFakta.toList().deepCopy(indeks, søknad).toMutableSet(),
                        avhengerAvFakta.toList().deepCopy(indeks, søknad).toMutableSet(),
                        mutableSetOf(),
                        roller
                    )
                )
            }
        }
    }

    internal fun tilbakestill() {
        seksjoner.forEach { seksjon -> seksjon.tilbakestill(faktumId) }
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*> {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId]!!
        val avhengigheter = avhengigeFakta.map { it.bygg(byggetFakta) }.toMutableSet()
        return TemplateFaktum(faktumId, navn, clazz, avhengigheter, avhengerAvFakta, roller)
            .also { byggetFakta[faktumId] = it }
    }
}
