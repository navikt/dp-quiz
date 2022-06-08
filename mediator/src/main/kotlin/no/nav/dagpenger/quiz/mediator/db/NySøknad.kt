package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Prosessversjon
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.visitor.SøknadVisitor
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import java.util.UUID

class NySøknad(søknad: Søknad, private val type: Versjon.UserInterfaceType) : SøknadVisitor {
    private var søknadId = 0
    private var internVersjonId = 0
    private var rootId = 0
    private var indeks = 0
    private val faktumList = mutableListOf<Faktum<*>>()
    private var personId: UUID? = null

    init {
        if (SøknadUuid(søknad).ikkeEksisterer()) {
            søknad.accept(this)
        }
    }

    override fun preVisit(person: Person, uuid: UUID) {
        personId = uuid
    }

    private fun hentInternId(prosessVersjon: Prosessversjon): Int {
        val query = queryOf( //language=PostgreSQL
            "SELECT id FROM V1_PROSESSVERSJON WHERE navn = :navn AND versjon_id = :versjon_id",
            mapOf("navn" to prosessVersjon.prosessnavn.id, "versjon_id" to prosessVersjon.versjon)
        )
        return using(sessionOf(dataSource)) { session ->
            session.run(
                query.map { it.intOrNull("id") }.asSingle
            ) ?: throw IllegalStateException("Fant ikke internid for prosessversjon $prosessVersjon")
        }
    }

    override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
        this.internVersjonId = hentInternId(prosessVersjon)
        søknadId = using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """
                        INSERT INTO soknad(uuid, versjon_id, person_id, sesjon_type_id) 
                        VALUES (:uuid, :versjon_id, :person_id, :sesjon_type_id) 
                        RETURNING id
                    """.trimIndent(),
                    mapOf(
                        "uuid" to uuid,
                        "versjon_id" to internVersjonId,
                        "person_id" to personId,
                        "sesjon_type_id" to type.id
                    )
                ).map { it.int(1) }.asSingle
            ) ?: throw IllegalArgumentException("failed to find søknadId")
        }
    }

    override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
        this.rootId = rootId
        this.indeks = indeks
    }

    override fun <R : Comparable<R>> visitUtenSvar(
        faktum: GrunnleggendeFaktum<R>,
        tilstand: Faktum.FaktumTilstand,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        godkjenner: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        gyldigeValg: GyldigeValg?,
        landGrupper: LandGrupper?
    ) {
        skrivFaktumVerdi(faktum)
    }

    override fun <R : Comparable<R>> visitUtenSvar(
        faktum: GeneratorFaktum,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        templates: List<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
    ) {
        skrivFaktumVerdi(faktum)
    }

    override fun <R : Comparable<R>> visit(
        faktum: TemplateFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>,
        gyldigeValg: GyldigeValg?
    ) {
        skrivFaktumVerdi(faktum)
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
        skrivFaktumVerdi(faktum)
    }

    private fun skrivFaktumVerdi(faktum: Faktum<*>) {
        if (faktum in faktumList) return else faktumList.add(faktum)
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    """INSERT INTO faktum_verdi
                                (soknad_id, indeks, faktum_id)
                            VALUES (?, ?,
                                    (SELECT id FROM faktum WHERE versjon_id = ? AND root_id = ?))""".trimMargin(),
                    søknadId,
                    indeks,
                    internVersjonId,
                    rootId
                ).asExecute
            )
        }
    }

    private class SøknadUuid(søknad: Søknad) : SøknadVisitor {

        init {
            søknad.accept(this)
        }

        private var eksisterer = false

        fun ikkeEksisterer() = !eksisterer

        override fun preVisit(søknad: Søknad, prosessVersjon: Prosessversjon, uuid: UUID) {
            eksisterer = eksisterer(uuid)
        }

        private companion object {
            private fun eksisterer(uuid: UUID): Boolean {
                val query = queryOf( //language=PostgreSQL
                    "SELECT id FROM soknad WHERE uuid = :uuid",
                    mapOf("uuid" to uuid)
                )
                return using(sessionOf(dataSource)) { session ->
                    session.run(
                        query.map { true }.asSingle
                    ) ?: false
                }
            }
        }
    }
}
