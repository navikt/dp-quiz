package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumNavn
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.util.UUID

class Søknad private constructor(private val uuid: UUID, private val seksjoner: MutableList<Seksjon>) : MutableList<Seksjon> by seksjoner {
    constructor(vararg seksjoner: Seksjon) : this(UUID.randomUUID(), seksjoner.toMutableList())

    internal val fakta: MutableMap<FaktumNavn, Faktum<*>>

    init {
        seksjoner.forEach {
            it.søknad(this)
        }
        fakta = MapBuilder(this).resultat
    }

    infix fun nesteSeksjon(subsumsjon: Subsumsjon) = seksjoner.first { subsumsjon.nesteFakta() in it }

    fun accept(visitor: SøknadVisitor) {
        visitor.preVisit(this, uuid)
        seksjoner.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }

    internal fun faktaMap(): Map<FaktumNavn, Faktum<*>> {
        return seksjoner.fold(mapOf()) { resultater, seksjon ->
            resultater + seksjon.faktaMap()
        }
    }

    private class MapBuilder(søknad: Søknad) : SøknadVisitor {
        val resultat = mutableMapOf<FaktumNavn, Faktum<*>>()
        init {
            søknad.accept(this)
        }

        override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>) {
            resultat[faktum.navn] = faktum
        }

        override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>, svar: R) {
            resultat[faktum.navn] = faktum
        }

        override fun <R : Comparable<R>> visit(faktum: TemplateFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
            resultat[faktum.navn] = faktum
        }

        override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
            resultat[faktum.navn] = faktum
        }

        override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>, svar: R) {
            resultat[faktum.navn] = faktum
        }

        override fun <R : Comparable<R>> visit(faktum: GeneratorFaktum, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
            resultat[faktum.navn] = faktum
        }
    }
}
