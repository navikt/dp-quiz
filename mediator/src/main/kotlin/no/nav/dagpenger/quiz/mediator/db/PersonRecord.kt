package no.nav.dagpenger.quiz.mediator.db

import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Person
import no.nav.dagpenger.model.visitor.PersonVisitor
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import java.util.UUID

internal class PersonRecord {

    internal fun hentEllerOpprettPerson(identer: Identer): Person {
        return FinnPersonVisitor(identer).person
    }

    internal fun hentPerson(personId: UUID): Person {
        val identBuilder = Identer.Builder()
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT verdi, historisk FROM folkeregisterident WHERE person_id = ?",
                    personId
                ).map { row -> identBuilder.folkeregisterIdent(row.string("verdi"), row.boolean("historisk")) }.asSingle
            )
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT verdi, historisk FROM aktoer_id WHERE person_id = ?",
                    personId
                ).map { row -> identBuilder.aktørId(row.string("verdi"), row.boolean("historisk")) }.asSingle
            )
            session.run(
                queryOf( //language=PostgreSQL
                    "SELECT verdi, historisk FROM np_id WHERE person_id = ?",
                    personId
                ).map { row -> identBuilder.npId(row.string("verdi"), row.boolean("historisk")) }.asSingle
            )
        }
        return Person(personId, identBuilder.build())
    }

    private class FinnPersonVisitor(private val identer: Identer) : PersonVisitor {

        lateinit var person: Person

        val folkeregisteridenter = mutableListOf<Identer.Ident>()
        val aktoerIder = mutableListOf<Identer.Ident>()
        val npIder = mutableListOf<Identer.Ident>()

        init {
            Person(identer).also { it.accept(this) }
        }

        override fun postVisit(person: Person, uuid: UUID) {

            val sql =
                """
                (SELECT person_id FROM folkeregisterident
                WHERE verdi in (${if (folkeregisteridenter.isEmpty()) null else folkeregisteridenter.joinToString { "?" }}))
                UNION 
                (SELECT person_id FROM aktoer_id
                WHERE verdi in (${if (aktoerIder.isEmpty()) null else aktoerIder.joinToString { "?" }}))
                UNION 
                (SELECT person_id FROM np_id
                WHERE verdi in (${if (npIder.isEmpty()) null else npIder.joinToString { "?" }}))
                """

            val personId = using(sessionOf(dataSource)) { session ->
                session.run(
                    queryOf(
                        sql,
                        *folkeregisteridenter.map { it.id }.toTypedArray(),
                        *aktoerIder.map { it.id }.toTypedArray(),
                    ).map { row ->
                        UUID.fromString(row.string(1))
                    }.asSingle
                ) ?: session.transaction { transaction ->
                    val personUuid = UUID.randomUUID()
                    transaction.run( //language=PostgreSQL
                        queryOf("""INSERT INTO person (uuid) VALUES (?)""", personUuid).asUpdate
                    )

                    folkeregisteridenter.forEach { ident ->
                        transaction.run( //language=PostgreSQL
                            queryOf(
                                """INSERT INTO folkeregisterident VALUES (?, ?, ?)""",
                                personUuid,
                                ident.id,
                                ident.historisk
                            ).asUpdate
                        )
                    }

                    aktoerIder.forEach { ident ->
                        transaction.run( //language=PostgreSQL
                            queryOf(
                                """INSERT INTO aktoer_id VALUES (?, ?, ?)""",
                                personUuid,
                                ident.id,
                                ident.historisk
                            ).asUpdate
                        )
                    }

                    npIder.forEach { ident ->
                        transaction.run( //language=PostgreSQL
                            queryOf(
                                """INSERT INTO np_id VALUES (?, ?, ?)""",
                                personUuid,
                                ident.id,
                                ident.historisk
                            ).asUpdate
                        )
                    }
                    personUuid
                }
            }
            this.person = Person(personId, identer)
        }

        override fun visit(type: Identer.Ident.Type, id: String, historisk: Boolean) {
            when (type) {
                Identer.Ident.Type.FOLKEREGISTERIDENT -> folkeregisteridenter.add(Identer.Ident(type, id, historisk))
                Identer.Ident.Type.AKTØRID -> aktoerIder.add(Identer.Ident(type, id, historisk))
                Identer.Ident.Type.NPID -> npIder.add(Identer.Ident(type, id, historisk))
                else -> throw IllegalArgumentException("Ukjent identtype: $type")
            }
        }
    }
}
