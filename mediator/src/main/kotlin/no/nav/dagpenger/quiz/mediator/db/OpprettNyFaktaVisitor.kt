package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.GyldigeValg
import no.nav.dagpenger.model.faktum.HenvendelsesType
import no.nav.dagpenger.model.faktum.LandGrupper
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.visitor.FaktaVisitor
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import java.util.UUID

class OpprettNyFaktaVisitor(fakta: Fakta) : FaktaVisitor {
    private var internVersjonId = 0
    private var rootId = 0
    private var indeks = 0
    private val faktumList = mutableListOf<Faktum<*>>()
    private var personId: UUID? = null
    private lateinit var søknadUUID: UUID
    private val faktumParametre = mutableListOf<Map<String, Any>>()

    init {
        fakta.accept(this)
    }

    override fun preVisit(person: Person, uuid: UUID) {
        personId = uuid
    }

    private fun hentInternId(prosessVersjon: HenvendelsesType): Int {
        val query = queryOf( //language=PostgreSQL
            "SELECT id FROM v1_prosessversjon WHERE navn = :navn AND versjon_id = :versjon_id",
            mapOf("navn" to prosessVersjon.prosessnavn.id, "versjon_id" to prosessVersjon.versjon)
        )
        return using(sessionOf(dataSource)) { session ->
            session.run(
                query.map { it.intOrNull("id") }.asSingle
            ) ?: throw IllegalStateException("Fant ikke internid for prosessversjon $prosessVersjon")
        }
    }

    override fun preVisit(fakta: Fakta, henvendelsesType: HenvendelsesType, uuid: UUID) {
        this.internVersjonId = hentInternId(henvendelsesType)
        this.søknadUUID = uuid
    }

    override fun visit(faktumId: FaktumId, rootId: Int, indeks: Int) {
        this.rootId = rootId
        this.indeks = indeks
    }

    override fun postVisit(fakta: Fakta, uuid: UUID) {
        val faktumInsertStatement = //language=PostgreSQL
            """INSERT INTO faktum_verdi
                                (soknad_id, indeks, faktum_id)
                            VALUES (:soknadId, :indeks,
                                    (SELECT id FROM faktum WHERE versjon_id = :internVersjonId AND root_id = :rootId))
            """.trimMargin()
        using(sessionOf(dataSource)) { session ->
            session.transaction { transactionalSession ->
                val id = transactionalSession.run(
                    queryOf( //language=PostgreSQL
                        """
                         INSERT INTO soknad(uuid, versjon_id, person_id) 
                         VALUES (:uuid, :versjon_id, :person_id) 
                         RETURNING id                        
                        """.trimIndent(),
                        mapOf(
                            "uuid" to søknadUUID,
                            "versjon_id" to internVersjonId,
                            "person_id" to personId
                        )
                    ).map { it.long(1) }.asSingle
                )
                val params = faktumParametre.map { originalParameter -> mapOf("soknadId" to id) + originalParameter }
                transactionalSession.batchPreparedNamedStatement(faktumInsertStatement, params)
            }
        }
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
        templates: List<TemplateFaktum<*>>,
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
        faktumParametre.add(
            mapOf(
                "indeks" to indeks,
                "internVersjonId" to internVersjonId,
                "rootId" to rootId
            )
        )
    }
}
