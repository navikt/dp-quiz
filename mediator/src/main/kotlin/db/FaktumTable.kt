package db

import DataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.BaseFaktumFactory
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.ja
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.fakta.Dokument
import no.nav.dagpenger.model.fakta.Fakta
import no.nav.dagpenger.model.fakta.Faktum
import no.nav.dagpenger.model.fakta.FaktumId
import no.nav.dagpenger.model.fakta.GeneratorFaktum
import no.nav.dagpenger.model.fakta.GrunnleggendeFaktum
import no.nav.dagpenger.model.fakta.Inntekt
import no.nav.dagpenger.model.fakta.Rolle
import no.nav.dagpenger.model.fakta.TemplateFaktum
import no.nav.dagpenger.model.fakta.UtledetFaktum
import no.nav.dagpenger.model.visitor.FaktaVisitor
import java.time.LocalDate
import java.util.UUID

// Forstår initialisering av faktum tabellen
class FaktumTable(fakta: Fakta, private val versjonId: Int) : FaktaVisitor {

    private var rootId: Int = 0
    private var indeks: Int = 0
    private val dbIder = mutableMapOf<Faktum<*>, Int>()
    private val avhengigheter = mutableMapOf<Faktum<*>, Set<Faktum<*>>>()

    init {
        if (!exists(versjonId)) fakta.accept(this)
    }

    companion object {
        private fun exists(versjonId: Int): Boolean {
            val query = queryOf("SELECT VERSJON_ID FROM FAKTUM WHERE VERSJON_ID = $versjonId")
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

    override fun <R : Comparable<R>> visit(faktum: GrunnleggendeFaktum<R>, tilstand: Faktum.FaktumTilstand, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
        skrivFaktum(faktum, clazz)
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun <R : Comparable<R>> visit(faktum: GeneratorFaktum, id: String, avhengigeFakta: Set<Faktum<*>>, templates: List<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
        skrivFaktum(faktum, clazz)
        faktumFaktum(skrivFaktum(faktum, clazz), templates, "template_faktum")
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun <R : Comparable<R>> visit(faktum: TemplateFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, roller: Set<Rolle>, clazz: Class<R>) {
        skrivFaktum(faktum, clazz)
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun <R : Comparable<R>> preVisit(faktum: UtledetFaktum<R>, id: String, avhengigeFakta: Set<Faktum<*>>, children: Set<Faktum<*>>, clazz: Class<R>, regel: FaktaRegel<R>) {
        if (dbIder.containsKey(faktum)) return
        faktumFaktum(skrivFaktum(faktum, clazz), children, "utledet_faktum")
        avhengigheter[faktum] = avhengigeFakta
    }

    override fun postVisit(fakta: Fakta, fnr: String, versjonId: Int, uuid: UUID) {
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

    private fun <R : Comparable<R>> skrivFaktum(faktum: Faktum<*>, clazz: Class<R>, regel: FaktaRegel<R>? = null) = if (dbIder.containsKey(faktum)) dbIder[faktum]!! else
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
