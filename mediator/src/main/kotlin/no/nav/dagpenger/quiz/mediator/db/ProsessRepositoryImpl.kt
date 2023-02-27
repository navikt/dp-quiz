package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.model.faktum.Faktatype
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.seksjon.Prosess
import no.nav.dagpenger.model.seksjon.Prosesstype
import no.nav.dagpenger.model.seksjon.Prosessversjon
import no.nav.dagpenger.model.visitor.PersonVisitor
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import java.util.UUID

class ProsessRepositoryImpl : ProsessRepository {
    private val personRecord = PersonRecord()
    private val faktaRepository = FaktaRecord()

    override fun ny(identer: Identer, prosesstype: Prosesstype, uuid: UUID, faktaUUID: UUID): Prosess {
        val person = personRecord.hentEllerOpprettPerson(identer)

        return Prosessversjon.id(prosesstype).utredningsprosess(person, uuid, faktaUUID).also {
            // TODO: Lag en form for retry om UUID finnes
            faktaRepository.ny(it.fakta)
            sessionOf(dataSource).use { session ->
                session.run(
                    queryOf(
                        //language=PostgreSQL
                        """
                        INSERT INTO prosess (person_id, navn, faktatype, uuid, fakta_id)
                        VALUES (:personId, :navn, :faktatype, :uuid, :faktaId)
                        ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        mapOf(
                            "personId" to PersonIdent(person).personId,
                            "navn" to prosesstype.navn,
                            "faktatype" to prosesstype.faktatype.id,
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
                    SELECT prosess.person_id, prosess.navn, prosess.faktatype, prosess.fakta_id
                    FROM prosess 
                    WHERE uuid = ?
                    """.trimMargin(),
                    uuid,
                ).map { row ->
                    ProsessRad(row.uuid("person_id"), row.string("navn"), row.string("faktatype"), row.uuid("fakta_id"))
                }.asSingle,
            )
        } ?: throw IllegalArgumentException("Kan ikke hente en utredningsprosess som ikke finnes, uuid: $uuid")
        val utredningsprosess =
            Prosessversjon.id(rad.prosesstype).utredningsprosess(PersonRecord().hentPerson(rad.personId), uuid, rad.faktaUUID)
        return faktaRepository.rehydrerProsess(utredningsprosess)
    }

    override fun lagre(prosess: Prosess) = faktaRepository.lagre(prosess.fakta)

    private data class ProsessFakta(override val id: String) : Faktatype
    private data class ProsessType(override val navn: String, override val faktatype: Faktatype) : Prosesstype
    private data class ProsessRad(val personId: UUID, val navn: String, val faktatype: String, val faktaUUID: UUID) {
        val prosesstype = ProsessType(navn, ProsessFakta(faktatype))
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
