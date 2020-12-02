package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.factory.FaktaRegel.Companion.VALG
import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.faktum.ValgFaktum
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.time.LocalDate
import java.util.UUID

// Forstår initialisering av faktum tabellen
class FaktumTable(søknad: Søknad, private val versjonId: Int) : SøknadVisitor {

    private var rootId: Int = 0
    private var indeks: Int = 0
    private val dbIder = mutableMapOf<Faktum<*>, Int>()
    private val avhengigheter = mutableMapOf<Faktum<*>, Set<Faktum<*>>>()

    init {
        if (!exists(versjonId)) søknad.accept(this)
    }

    companion object {
        private fun exists(versjonId: Int): Boolean {
            val query = queryOf("SELECT versjon_id FROM faktum WHERE versjon_id = $versjonId")
            return using(sessionOf(dataSource)) { session ->
                session.run(
                    query.map { true }.asSingle
                ) ?: false
            }
        }
    }

    override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
        this.rootId = rootId
        this.indeks = indeks
    }

    override fun <R : Comparable<R>> visit(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        skrivFaktum(faktum, clazz)
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun <R : Comparable<R>> visit(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        skrivFaktum(faktum, clazz)
        faktumFaktum(skrivFaktum(faktum, clazz), templates, "template_faktum")
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun <R : Comparable<R>> visit(
        faktum: TemplateFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        skrivFaktum(faktum, clazz)
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun <R : Comparable<R>> preVisit(
        faktum: UtledetFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        children: Set<Faktum<*>>,
        clazz: Class<R>,
        regel: FaktaRegel<R>
    ) {
        if (dbIder.containsKey(faktum)) return
        faktumFaktum(skrivFaktum(faktum, clazz, regel), children, "utledet_faktum")
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun preVisit(
        faktum: ValgFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        underordnedeJa: Set<Faktum<Boolean>>,
        underordnedeNei: Set<Faktum<Boolean>>,
        clazz: Class<Boolean>
    ) {
        if (dbIder.containsKey(faktum)) return
        skrivFaktum(faktum, clazz, VALG).also {
            valgFaktum(it, underordnedeJa, true)
            valgFaktum(it, underordnedeNei, false)
        }
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun postVisit(søknad: Søknad, versjonId: Int, uuid: UUID) {
        avhengigheter.forEach { (parent, children) -> faktumFaktum(dbIder[parent]!!, children, "avhengig_faktum") }
    }

    private fun faktumFaktum(parentId: Int, children: Collection<Faktum<*>>, table: String) {
        children.forEach { child ->
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "INSERT INTO $table (parent_id, child_id) VALUES (?, ?)".trimMargin(),
                        parentId,
                        dbIder[child]
                    ).asExecute
                )
            }
        }
    }

    private fun valgFaktum(parentId: Int, children: Collection<Faktum<*>>, type: Boolean) {
        children.forEach { child ->
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        "INSERT INTO valg_faktum (parent_id, child_id, ja_nei) VALUES (?, ?, ?)".trimMargin(),
                        parentId,
                        dbIder[child],
                        type
                    ).asExecute
                )
            }
        }
    }

    private fun <R : Comparable<R>> skrivFaktum(faktum: Faktum<*>, clazz: Class<R>, regel: FaktaRegel<R>? = null) =
        if (dbIder.containsKey(faktum)) dbIder[faktum]!! else
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        """WITH inserted_id as (INSERT INTO navn (navn) values (?) returning id)
                               INSERT INTO faktum (versjon_id, faktum_type, root_id, regel, navn_id ) SELECT ?, ?, ?, ?, id from inserted_id returning id""".trimMargin(),
                        faktum.navn,
                        versjonId,
                        ClassKode[clazz],
                        rootId,
                        regel?.navn
                    ).map { it.int(1) }.asSingle
                )
            }!!.also { dbId ->
                dbIder[faktum] = dbId
            }

    // Understands an encoding of basic Faktum types
    internal class ClassKode() {
        companion object {
            private val factoryMap = mutableMapOf<Int, (String, Int) -> FaktumFactory<*>>()
            private val kodeMap = mutableMapOf<Class<*>, Int>()

            private fun byggMap(clazz: Class<*>, kode: Int, block: (String, Int) -> FaktumFactory<*>) {
                factoryMap[kode] = block
                kodeMap[clazz] = kode
            }

            init {
                byggMap(Int::class.java, 1) { navn, rootId -> heltall faktum navn id rootId }
                byggMap(Boolean::class.java, 2) { navn, rootId -> ja nei navn id rootId }
                byggMap(LocalDate::class.java, 3) { navn, rootId -> dato faktum navn id rootId }
                byggMap(Dokument::class.java, 4) { navn, rootId -> dokument faktum navn id rootId }
                byggMap(Inntekt::class.java, 5) { navn, rootId -> inntekt faktum navn id rootId }
            }

            operator fun get(clazz: Class<*>) = kodeMap[clazz] ?: throw NoSuchElementException("Ukjent klasse $clazz")
            operator fun get(kode: Int) = factoryMap[kode] ?: throw NoSuchElementException("Ukjent kode $kode")
        }
    }
}
