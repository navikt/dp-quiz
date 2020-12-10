package no.nav.dagpenger.quiz.mediator.db

import PostgresDataSourceBuilder.dataSource
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.factory.FaktaRegel
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.GeneratorFaktum
import no.nav.dagpenger.model.faktum.GrunnleggendeFaktum
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.faktum.Rolle
import no.nav.dagpenger.model.faktum.Søknad
import no.nav.dagpenger.model.faktum.TemplateFaktum
import no.nav.dagpenger.model.faktum.UtledetFaktum
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.model.visitor.SøknadVisitor
import java.util.UUID

class NySøknad(søknad: Søknad, private val type: Versjon.UserInterfaceType) : SøknadVisitor {
    private var søknadId = 0
    private var versjonId = 0
    private var rootId = 0
    private var indeks = 0
    private val faktumList = mutableListOf<Faktum<*>>()
    private var personId: UUID? = null

    init {
        søknad.accept(this)
    }

    override fun preVisit(person: Person, uuid: UUID) {
        personId = uuid
    }

    override fun preVisit(søknad: Søknad, versjonId: Int, uuid: UUID) {
        this.versjonId = versjonId
        søknadId = using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    "INSERT INTO soknad(uuid, versjon_id, person_id, sesjon_type_id) VALUES (?, ?, ?, ?) returning id",
                    uuid,
                    versjonId,
                    personId,
                    type.id
                ).map { it.int(1) }.asSingle
            ) ?: throw IllegalArgumentException("failed to find søknadId")
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
        skrivFaktumVerdi(faktum)
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
        skrivFaktumVerdi(faktum)
    }

    override fun <R : Comparable<R>> visit(
        faktum: TemplateFaktum<R>,
        id: String,
        avhengigeFakta: Set<Faktum<*>>,
        avhengerAvFakta: Set<Faktum<*>>,
        roller: Set<Rolle>,
        clazz: Class<R>
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
                    versjonId,
                    rootId
                ).asExecute
            )
        }
    }
}
