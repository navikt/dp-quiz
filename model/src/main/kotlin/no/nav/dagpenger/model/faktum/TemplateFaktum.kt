package no.nav.dagpenger.model.faktum

import no.nav.dagpenger.model.seksjon.Seksjon
import no.nav.dagpenger.model.seksjon.Seksjon.Companion.medSøknadprosess
import no.nav.dagpenger.model.visitor.FaktumVisitor

class TemplateFaktum<R : Comparable<R>> internal constructor(
    faktumId: FaktumId,
    navn: String,
    internal val clazz: Class<R>,
    avhengigeFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    avhengerAvFakta: MutableSet<Faktum<*>> = mutableSetOf(),
    roller: MutableSet<Rolle> = mutableSetOf(),
    private val gyldigeValg: GyldigeValg? = null,
    private val landgrupper: LandGrupper? = null
) : Faktum<R>(faktumId, navn, avhengigeFakta, avhengerAvFakta, roller) {
    private val seksjoner = mutableListOf<Seksjon>()

    override fun type() = clazz

    override fun tilUbesvart() {
        // Ignorert
    }

    override fun svar(): R {
        throw IllegalStateException("Templates har ikke svar")
    }

    override fun besvartAv(): String? {
        throw IllegalStateException("Templates har ikke svar")
    }

    override fun grunnleggendeFakta(): Set<GrunnleggendeFaktum<*>> = emptySet()

    override fun leggTilHvis(kode: FaktumTilstand, fakta: MutableSet<GrunnleggendeFaktum<*>>) {
        // Ignorert
    }

    override fun erBesvart() = false

    override fun accept(visitor: FaktumVisitor) {
        faktumId.accept(visitor)
        visitor.visit(this, id, avhengigeFakta, avhengerAvFakta, roller, clazz, gyldigeValg)
    }

    override fun add(seksjon: Seksjon) =
        seksjoner.add(seksjon)

    override fun deepCopy(indeks: Int, søknad: Søknad): Faktum<*> {
        return søknad.idOrNull(faktumId medIndeks indeks)
            ?: deepCopy(indeks)
                .also { lagdFaktum ->
                    søknad.add(lagdFaktum)
                    leggTilAvhengighet(søknad, indeks, lagdFaktum)
                }
    }

    private fun deepCopy(indeks: Int) = GrunnleggendeFaktum(
        faktumId medIndeks indeks,
        navn,
        clazz,
        mutableSetOf(),
        mutableSetOf(),
        mutableSetOf(),
        roller,
        gyldigeValg,
        landgrupper
    )

    // Lag instans av template faktum ved besvar eller rehydrering av søknad
    internal fun generate(generator: Int, søknad: Søknad) {
        // Seksjoner dette templatet ligger i
        seksjoner.forEach { originalSeksjon ->
            generator.forHvertSvar { indeks: Int ->
                // Sjekker om seksjonen er *kun* templates, og skal dermed klones før vi lager instanser av template i den
                val seksjon = instansiertSeksjon(originalSeksjon, indeks)
                deepCopy(indeks).also { lagdFaktum ->
                    seksjon.add(lagdFaktum)
                    leggTilAvhengighet(søknad, indeks, lagdFaktum)
                }
            }
        }
    }

    private fun leggTilAvhengighet(
        søknad: Søknad,
        indeks: Int,
        lagdFaktum: GrunnleggendeFaktum<R>
    ) {
        avhengerAvFakta.map { avhengighet ->
            søknad.finnEksisterende(avhengighet) ?: avhengighet.deepCopy(indeks, søknad)
        }.forEach { it.leggTilAvhengighet(lagdFaktum) }
    }

    // Finner et allerede instansiert faktum, eller lager ett nytt
    private fun Søknad.finnEksisterende(avhengighet: Faktum<*>) = when (avhengighet) {
        is TemplateFaktum<*> -> singleOrNull {
            it.faktumId.generertFra(avhengighet.faktumId)
        }
        else -> idOrNull(avhengighet.faktumId)
    }

    // Sjekker om seksjonen er *kun* templates, og skal dermed klones før vi lager instanser av template i den
    private fun instansiertSeksjon(
        originalSeksjon: Seksjon,
        indeks: Int
    ) = if (originalSeksjon.bareTemplates()) {
        originalSeksjon.deepCopy(indeks)
    } else {
        originalSeksjon
    }

    private fun Int.forHvertSvar(block: (Int) -> Unit) = (1..this).forEach { block(it) }

    internal fun tilbakestill() {
        seksjoner.medSøknadprosess().forEach { it.tilbakestill(faktumId) }
    }

    override fun bygg(byggetFakta: MutableMap<FaktumId, Faktum<*>>): Faktum<*> {
        if (byggetFakta.containsKey(faktumId)) return byggetFakta[faktumId]!!
        val avhengigheter = avhengigeFakta.map { it.bygg(byggetFakta) }.toMutableSet()
        return TemplateFaktum(faktumId, navn, clazz, avhengigheter, avhengerAvFakta, roller, gyldigeValg, landgrupper)
            .also { byggetFakta[faktumId] = it }
    }
}
