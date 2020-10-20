package no.nav.dagpenger.model.søknad

import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.subsumsjon.Subsumsjon
import no.nav.dagpenger.model.subsumsjon.TomSubsumsjon
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.util.UUID

class Søknad private constructor(
    internal val fakta2: Fakta,
    private val rootSubsumsjon: Subsumsjon,
    private val uuid: UUID,
    private val seksjoner: MutableList<Seksjon>
) : MutableList<Seksjon> by seksjoner {

    constructor(vararg seksjoner: Seksjon) : this(
        Fakta("", mutableMapOf()),
        TomSubsumsjon,
        UUID.randomUUID(),
        seksjoner.toMutableList()
    )

    constructor(fakta: Fakta, rootSubsumsjon: Subsumsjon, vararg seksjoner: Seksjon) : this(
        fakta,
        rootSubsumsjon,
        UUID.randomUUID(),
        seksjoner.toMutableList()
    )

    internal val fakta: MutableMap<FaktumId, Faktum<*>>

    init {
        seksjoner.forEach {
            it.søknad(this)
        }
        fakta = MapBuilder(this).resultat
    }

    fun <T : Comparable<T>> finnFaktum(id: String) = (fakta[FaktumId(id)] as Faktum<T>)

    infix fun nesteSeksjon(subsumsjon: Subsumsjon) = seksjoner.first { subsumsjon.nesteFakta() in it }

    fun accept(visitor: SøknadVisitor) {
        visitor.preVisit(this, uuid)
        seksjoner.forEach { it.accept(visitor) }
        visitor.postVisit(this)
    }

    internal fun faktum(id: FaktumId) =
        fakta[id] ?: throw IllegalArgumentException("Faktum med denne id-en finnes ikke, id ${id.id}")

    fun seksjon(navn: String) = seksjoner.first { it.navn == navn }

    private class MapBuilder(søknad: Søknad) : SøknadVisitor {
        val resultat = mutableMapOf<FaktumId, Faktum<*>>()
        init {
            søknad.accept(this)
        }

        private fun set(faktum: Faktum<*>) {
            if (resultat.containsKey(faktum.faktumId) && resultat[faktum.faktumId] != faktum) throw IllegalArgumentException("Duplisert faktumnavn i søknad: ${faktum.faktumId}")
            resultat[faktum.faktumId] = faktum
        }

        override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>) {
            set(faktum)
        }

        override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>, svar: R) {
            set(faktum)
        }

        override fun <R : Comparable<R>> visit(faktum: TemplateFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
            set(faktum)
        }

        override fun <R : Comparable<R>> visit(
            faktum: GrunnleggendeFaktum<R>,
            tilstand: Faktum.FaktumTilstand,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>
        ) {
            set(faktum)
        }

        override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>, svar: R) {
            set(faktum)
        }

        override fun <R : Comparable<R>> visit(
            faktum: GeneratorFaktum,
            id: String,
            avhengigeFakta: Set<Faktum<*>>,
            templates: List<Faktum<*>>,
            roller: Set<Rolle>,
            clazz: Class<R>
        ) {
            set(faktum)
        }
    }
}

private fun String.rootId() = this.toInt()
private fun String.indeks(): Int? = this.toInt()
