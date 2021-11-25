package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.boolsk
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dato
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.desimaltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.dokument
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.heltall
import no.nav.dagpenger.model.factory.BaseFaktumFactory.Companion.inntekt
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.factory.FaktumFactory
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.visitor.SøknadVisitor
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import java.time.LocalDate
import java.util.UUID

// Forstår initialisering av faktum tabellen
class FaktumTable(søknad: Søknad) : SøknadVisitor {

    private var rootId: Int = 0
    private var indeks: Int = 0
    private val dbIder = mutableMapOf<Faktum<*>, Int>()
    private val avhengigheter = mutableMapOf<Faktum<*>, Set<Faktum<*>>>()
    private var prosessVersjonId = 0

    init {
        if (Versjonsjekker(søknad).ikkeEksisterer()) søknad.accept(this)
    }

    private class Versjonsjekker(søknad: Søknad) : SøknadVisitor {

        init {
            søknad.accept(this)
        }

        private var eksisterer = false

        fun ikkeEksisterer() = !eksisterer

        override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
            eksisterer = exists(prosessVersjon)
        }

        private companion object {
            private fun exists(prosessVersjon: Prosessversjon): Boolean {
                val query = queryOf( //language=PostgreSQL
                    "SELECT id FROM V1_PROSESSVERSJON WHERE navn = :navn AND versjon_id = :versjon_id",
                    mapOf("navn" to prosessVersjon.prosessnavn.id, "versjon_id" to prosessVersjon.versjon)
                )
                return using(sessionOf(dataSource)) { session ->
                    session.run(
                        query.map { true }.asSingle
                    ) ?: false
                }
            }
        }
    }

    override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        val query = queryOf( //language=PostgreSQL
            "INSERT INTO V1_PROSESSVERSJON (navn, versjon_id) VALUES (:navn, :versjon_id) RETURNING id",
            mapOf("navn" to prosessVersjon.prosessnavn.id, "versjon_id" to prosessVersjon.versjon)
        )
        prosessVersjonId = using(sessionOf(dataSource)) { session ->
            session.run(
                query.map { rad -> rad.int("id") }.asSingle
            ) ?: throw IllegalStateException("Klarte ikke å opprette prosessversjon, $prosessVersjon")
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

    override fun postVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
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

    private fun <R : Comparable<R>> skrivFaktum(faktum: Faktum<*>, clazz: Class<R>, regel: FaktaRegel<R>? = null) =
        if (dbIder.containsKey(faktum)) dbIder[faktum]!! else
            using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        """WITH inserted_id as (INSERT INTO navn (navn) values (?) returning id)
                               INSERT INTO faktum (versjon_id, faktum_type, root_id, regel, navn_id ) SELECT ?, ?, ?, ?, id from inserted_id returning id""".trimMargin(),
                        faktum.navn,
                        prosessVersjonId,
                        ClassKode[clazz],
                        rootId,
                        regel?.navn
                    ).map { it.int(1) }.asSingle
                )
            }!!.also { dbId ->
                dbIder[faktum] = dbId
            }

    // Understands an encoding of basic Faktum types
    internal class ClassKode {
        companion object {
            private val factoryMap = mutableMapOf<Int, (String, Int) -> FaktumFactory<*>>()
            private val kodeMap = mutableMapOf<Class<*>, Int>()

            private fun byggMap(clazz: Class<*>, kode: Int, block: (String, Int) -> FaktumFactory<*>) {
                factoryMap[kode] = block
                kodeMap[clazz] = kode
            }

            init {
                byggMap(Int::class.java, 1) { navn, rootId -> heltall faktum navn id rootId }
                byggMap(Boolean::class.java, 2) { navn, rootId -> boolsk faktum navn id rootId }
                byggMap(LocalDate::class.java, 3) { navn, rootId -> dato faktum navn id rootId }
                byggMap(Dokument::class.java, 4) { navn, rootId -> dokument faktum navn id rootId }
                byggMap(Inntekt::class.java, 5) { navn, rootId -> inntekt faktum navn id rootId }
                byggMap(Double::class.java, 6) { navn, rootId -> desimaltall faktum navn id rootId }
            }

            operator fun get(clazz: Class<*>) = kodeMap[clazz] ?: throw NoSuchElementException("Ukjent klasse $clazz")
            operator fun get(kode: Int) = factoryMap[kode] ?: throw NoSuchElementException("Ukjent kode $kode")
        }
    }
}
