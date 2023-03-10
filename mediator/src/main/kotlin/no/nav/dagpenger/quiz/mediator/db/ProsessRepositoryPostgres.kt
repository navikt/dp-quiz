package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Henvendelser
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosesstype
import no.nav.dagpenger.model.visitor.PersonVisitor
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import java.util.UUID

class ProsessRepositoryPostgres : ProsessRepository {
    private val personRecord = PersonRecord()
    private val faktaRepository = FaktaRecord()

    override fun ny(identer: Identer, prosesstype: Prosesstype, uuid: UUID, faktaUUID: UUID): Prosess {
        val person = personRecord.hentEllerOpprettPerson(identer)

        return Henvendelser.prosess(person, prosesstype, uuid, faktaUUID).also {
            // TODO: Lag en form for retry om UUID finnes
            faktaRepository.rehydrerEllerOpprett(it.fakta, person)
            sessionOf(dataSource).use { session ->
                session.run(
                    queryOf(
                        //language=PostgreSQL
                        """
                        INSERT INTO prosess (person_id, navn, uuid, fakta_id)
                        VALUES (:personId, :navn, :uuid, :faktaId)
                        ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        mapOf(
                            "personId" to PersonIdent(person).personId,
                            "navn" to prosesstype.navn,
                            "uuid" to uuid,
                            "faktaId" to faktaUUID,
                        ),
                    ).asExecute,
                )
            }
        }
    }

    override fun hent(uuid: UUID): Prosess {
        val rad = sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT p.person_id, p.navn, fv.navn AS faktatype, p.fakta_id, fv.versjon_id FROM prosess AS p 
                        JOIN fakta AS f ON f.uuid = p.fakta_id
                        JOIN faktaversjon AS fv ON f.versjon_id = fv.id
                        WHERE p.uuid = ?
                    """.trimMargin(),
                    uuid,
                ).map { row ->
                    ProsessRad(
                        personId = row.uuid("person_id"),
                        navn = row.string("navn"),
                        faktatype = row.string("faktatype"),
                        faktaUUID = row.uuid("fakta_id"),
                        versjonId = row.int("versjon_id"),
                    )
                }.asSingle,
            )
        } ?: throw IllegalArgumentException("Kan ikke hente en prosess som ikke finnes, uuid: $uuid")
        val person = PersonRecord().hentPerson(rad.personId)
        val prosess = Henvendelser.prosess(person, rad.prosesstype, uuid, rad.faktaUUID, rad.faktaversjon)
        faktaRepository.rehydrerFakta(prosess.fakta)

        return prosess
    }

    override fun lagre(prosess: Prosess) = faktaRepository.lagre(prosess.fakta)

    override fun slett(uuid: UUID) {
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    "DELETE FROM prosess WHERE uuid = :uuid",
                    mapOf("uuid" to uuid),
                ).asExecute,
            )
        }
    }

    private data class ProsessFakta(override val id: String) : Faktatype
    private data class ProsessType(override val navn: String, override val faktatype: Faktatype) : Prosesstype
    private data class ProsessRad(
        val personId: UUID,
        val navn: String,
        val faktatype: String,
        val faktaUUID: UUID,
        val versjonId: Int,
    ) {
        val prosesstype = ProsessType(navn, ProsessFakta(faktatype))
        val faktaversjon = Faktaversjon(ProsessFakta(faktatype), versjonId)
    }

    private class PersonIdent(person: Person) : PersonVisitor {
        lateinit var personId: UUID

        init {
            person.accept(this)
        }

        override fun preVisit(person: Person, uuid: UUID) {
            personId = uuid
        }
    }
}
