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

    internal val fakta: MutableMap<String, Faktum<*>>

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

    internal fun faktum(id: String): Faktum<*> =
        fakta[id].let {
            if (it != null) return it
            val regex = Regex("""^(\d+)\.(\d+)$""")
            val matchResult = regex.matchEntire(id) ?: throw IllegalArgumentException("Faktum med id $id finnes ikke i søknaden")
            val (templateId, indeks) = matchResult!!.destructured
            fakta[templateId]?.let { template ->
                if (template !is TemplateFaktum) throw IllegalArgumentException("Faktum med id $templateId må være et TemplateFaktum")
                template.tilFaktum(indeks.toInt()).also { generertFaktum ->
                    fakta[generertFaktum.id] = generertFaktum
                }
            } ?: throw IllegalArgumentException("Faktum med id $id finnes ikke i søknaden")
        }

    private class MapBuilder(søknad: Søknad) : SøknadVisitor {
        internal val resultat = mutableMapOf<String, Faktum<*>>()
        init {
            søknad.accept(this)
        }

        override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>) {
            resultat[faktum.id] = faktum
        }

        override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>, svar: R) {
            resultat[faktum.id] = faktum
        }

        override fun <R : Comparable<R>> visit(faktum: TemplateFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
            resultat[faktum.id] = faktum
        }

        override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
            resultat[faktum.id] = faktum
        }

        override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>, svar: R) {
            resultat[faktum.id] = faktum
        }

        override fun <R : Comparable<R>> visit(faktum: GeneratorFaktum, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
            resultat[faktum.id] = faktum
        }
    }
}
