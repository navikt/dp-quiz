package no.nav.dagpenger.quiz.mediator.db

import kotliquery.TransactionalSession
import kotliquery.action.ExecuteQueryAction
import kotliquery.action.UpdateQueryAction
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.model.faktum.Dokument
import no.nav.dagpenger.model.faktum.Envalg
import no.nav.dagpenger.model.faktum.Fakta
import no.nav.dagpenger.model.faktum.Faktaversjon
import no.nav.dagpenger.model.faktum.Faktum
import no.nav.dagpenger.model.faktum.FaktumId
import no.nav.dagpenger.model.faktum.Flervalg
import no.nav.dagpenger.model.faktum.Identer
import no.nav.dagpenger.model.faktum.Identer.Ident
import no.nav.dagpenger.model.faktum.Inntekt
import no.nav.dagpenger.model.faktum.Inntekt.Companion.årlig
import no.nav.dagpenger.model.faktum.Land
import no.nav.dagpenger.model.faktum.Periode
import no.nav.dagpenger.model.faktum.Prosessnavn
import no.nav.dagpenger.model.faktum.Tekst
import no.nav.dagpenger.model.seksjon.Utredningsprosess
import no.nav.dagpenger.model.seksjon.Versjon
import no.nav.dagpenger.quiz.mediator.db.PostgresDataSourceBuilder.dataSource
import java.math.BigInteger
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

// Understands a relational representation of a Fakta
class FaktaRecord : FaktaPersistence {
    private val personRecord = PersonRecord()

    private companion object {
        val logger = KotlinLogging.logger {}
        val sikkerlogg = KotlinLogging.logger("tjenestekall")
    }

    override fun ny(
        identer: Identer,
        prosessVersjon: Faktaversjon,
        uuid: UUID,
    ): Utredningsprosess {
        val person = personRecord.hentEllerOpprettPerson(identer)
        return Versjon.id(prosessVersjon).utredningsprosess(person, uuid).also { søknadprosess ->
            if (eksisterer(uuid)) return søknadprosess
            OpprettNyFaktaVisitor(søknadprosess.fakta)
        }
    }

    override fun eksisterer(uuid: UUID): Boolean {
        val query = queryOf(
            //language=PostgreSQL
            "SELECT id FROM soknad WHERE uuid = :uuid",
            mapOf("uuid" to uuid),
        )
        return using(sessionOf(dataSource)) { session ->
            session.run(
                query.map { true }.asSingle,
            ) ?: false
        }
    }

    private data class Prosess(override val id: String) : Prosessnavn

    override fun hent(uuid: UUID): Utredningsprosess {
        data class SoknadRad(val personId: UUID, val navn: String, val versjonId: Int)

        val rad = using(sessionOf(dataSource)) { session ->
            if (type != null) {
                session.run(
                    //language=PostgreSQL
                    queryOf("UPDATE soknad SET sesjon_type_id = ? WHERE uuid = ?", type.id, uuid).asUpdate,
                )
            }

            session.run(
                queryOf(
                    //language=PostgreSQL
                    "SELECT soknad.person_id, versjon.navn , versjon.versjon_id FROM soknad JOIN v1_prosessversjon AS versjon ON (versjon.id = soknad.versjon_id) WHERE uuid = ?",
                    uuid,
                ).map { row ->
                    SoknadRad(UUID.fromString(row.string(1)), row.string(2), row.int(3))
                }.asSingle,
            )
        } ?: throw IllegalArgumentException("Kan ikke hente en søknad som ikke finnes, uuid: $uuid")

        return Versjon.id(Faktaversjon(Prosess(rad.navn), rad.versjonId)).utredningsprosess(
            person = personRecord.hentPerson(rad.personId),
            faktaUUID = uuid,
        ).also { søknadprosess ->
            svarList(uuid).onEach { row ->
                søknadprosess.fakta.idOrNull(row.id)?.also { rehydrerFaktum(row, it) }
            }
        }
    }

    override fun lagre(fakta: Fakta): Boolean {
        val nyeSvar: MutableMap<String, Faktum<*>?> = svarMap(fakta)
        val originalSvar: MutableMap<String, Faktum<*>?> = svarMap(hent(fakta.uuid).fakta)
        using(sessionOf(dataSource)) { session ->
            session.transaction { transactionalSession ->
                slettDødeTemplatefakta(fakta, nyeSvar, originalSvar, transactionalSession)
                oppdaterEndretFaktum(originalSvar, nyeSvar, fakta, transactionalSession)
                skrivNyeFaktum(nyeSvar, originalSvar, fakta, transactionalSession)
            }
        }
        // TODO: burde ikke alltid returnere true?? hva hvis noe går galt som ikke kræsjer? hvorfor boolean?
        return true
    }

    override fun slett(uuid: UUID): Boolean {
        using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf( //language=PostgreSQL
                    "DELETE FROM soknad WHERE uuid = :uuid",
                    mapOf("uuid" to uuid),
                ).asExecute,
            )
        }
        return true
    }

    override fun migrer(uuid: UUID, tilVersjon: Faktaversjon?): Faktaversjon {
        val gjeldendeVersjon = prosessversjon(uuid)
        val nyVersjon = tilVersjon ?: gjeldendeVersjon.siste()

        if (!gjeldendeVersjon.kanMigrereTil(nyVersjon)) return gjeldendeVersjon
        val insertQuery = //language=PostgreSQL
            "INSERT INTO faktum_verdi (soknad_id, indeks, faktum_id) VALUES (:soknadId, 0, :id)"
        val updateQuery = //language=PostgreSQL
            "UPDATE faktum_verdi SET faktum_id = :nyFaktumId WHERE soknad_id = :soknadId::bigint AND faktum_id = :gammelFaktumId::bigint"
        val inserts = mutableListOf<Map<String, Any>>()
        val updates = mutableListOf<Map<String, Any>>()

        using(sessionOf(dataSource)) { session ->
            val gjeldendeTilstand = session.run(hentFaktum(gjeldendeVersjon)).associateBy { it.rootId }
            val ønsketTilstand = session.run(hentFaktum(nyVersjon))
            val soknadId = session.run(internSoknadId(uuid))!!
            logger.info { "Migrerer søknadId=$soknadId fra $gjeldendeVersjon til $nyVersjon" }
            ønsketTilstand.forEach { faktum ->
                val forrigeFaktum = gjeldendeTilstand[faktum.rootId]

                when (forrigeFaktum) {
                    null -> inserts.add(mapOf("soknadId" to soknadId, "id" to faktum.faktumId))
                    else -> updates.add(
                        mapOf(
                            "soknadId" to soknadId,
                            "gammelFaktumId" to forrigeFaktum.faktumId,
                            "nyFaktumId" to faktum.faktumId,
                        ),
                    )
                }
            }

            session.batchPreparedNamedStatement(insertQuery, inserts).also {
                logger.info { "Kjørte batched insert med ${it.size} inserts" }
            }
            session.batchPreparedNamedStatement(updateQuery, updates).also {
                logger.info { "Kjørte batched update med ${it.size} updates" }
            }

            session.run(settVersjon(soknadId, nyVersjon))
        }

        return nyVersjon
    }

    private data class FaktumMigrering(val faktumId: BigInteger, val rootId: Int) {
        fun opprettQuery(soknadId: BigInteger) = queryOf(
            //language=PostgreSQL
            "INSERT INTO faktum_verdi (soknad_id, indeks, faktum_id) VALUES (:soknadId, 0, :id)",
            mapOf("soknadId" to soknadId, "id" to faktumId),
        ).asUpdate

        fun oppdaterQuery(soknadId: BigInteger, gammelFaktumId: BigInteger, nyFaktumId: BigInteger) =
            queryOf(
                //language=PostgreSQL
                "UPDATE faktum_verdi SET faktum_id = :nyFaktumId WHERE soknad_id = :soknadId AND faktum_id = :gammelFaktumId ",
                mapOf("soknadId" to soknadId, "gammelFaktumId" to gammelFaktumId, "nyFaktumId" to nyFaktumId),
            ).asUpdate

        fun opprettEllerOppdater(forrigeFaktum: FaktumMigrering?, soknadId: BigInteger) = when (forrigeFaktum) {
            null -> opprettQuery(soknadId)
            else -> oppdaterQuery(soknadId, forrigeFaktum.faktumId, faktumId)
        }
    }

    private fun internSoknadId(uuid: UUID) =
        queryOf(
            // language=PostgreSQL
            "SELECT id FROM soknad WHERE uuid=?",
            uuid,
        ).map { it.bigDecimal("id").toBigInteger() }.asSingle

    private fun settVersjon(soknadId: BigInteger, versjon: Faktaversjon) =
        queryOf(
            // language=PostgreSQL
            """UPDATE soknad
            |SET versjon_id = (SELECT id FROM v1_prosessversjon WHERE navn = :navn AND versjon_id = :versjonId)
            |WHERE id = :soknadId
            """.trimMargin(),
            mapOf("navn" to versjon.prosessnavn.id, "versjonId" to versjon.versjon, "soknadId" to soknadId),
        ).asUpdate

    private fun hentFaktum(sisteVersjon: Faktaversjon) =
        queryOf(
            // language=PostgreSQL
            """
            |SELECT faktum.*, v1_prosessversjon.id AS prosessinternid
            |FROM faktum, v1_prosessversjon
            |WHERE faktum.versjon_id = v1_prosessversjon.id AND v1_prosessversjon.navn=? AND v1_prosessversjon.versjon_id=?
            """.trimMargin(),
            sisteVersjon.prosessnavn.id,
            sisteVersjon.versjon,
        ).map {
            FaktumMigrering(
                faktumId = it.bigDecimal("id").toBigInteger(),
                rootId = it.int("root_id"),
            )
        }.asList

    private fun prosessversjon(uuid: UUID) = using(sessionOf(dataSource)) { session ->
        session.run(
            queryOf(
                // language=PostgreSQL
                """SELECT v.navn, v.versjon_id 
                |FROM v1_prosessversjon v
                |LEFT JOIN soknad s ON v.id=s.versjon_id
                |WHERE s.uuid = :uuid
                """.trimMargin(),
                mapOf("uuid" to uuid),
            ).map { Faktaversjon(Prosess(it.string("navn")), it.int("versjon_id")) }.asSingle,
        )
    } ?: throw IllegalArgumentException("Kan ikke finne prosessversjon for en søknad som ikke finnes, uuid: $uuid")

    private fun skrivNyeFaktum(
        nyeSvar: MutableMap<String, Faktum<*>?>,
        originalSvar: MutableMap<String, Faktum<*>?>,
        fakta: Fakta,
        transactionalSession: TransactionalSession,
    ) {
        nyeSvar.filterNot { (id, _) -> originalSvar.containsKey(id) }.forEach { (id, svar) ->
            val (rootId, indeks) = fakta.id(id).reflection { rootId, indeks -> rootId to indeks }

            transactionalSession.run(opprettTemplateFaktum(indeks, fakta, rootId))
            if (svar != null) transactionalSession.run(oppdaterFaktum(svar, fakta, indeks, rootId))
        }
    }

    private fun oppdaterEndretFaktum(
        originalSvar: MutableMap<String, Faktum<*>?>,
        nyeSvar: MutableMap<String, Faktum<*>?>,
        fakta: Fakta,
        transactionalSession: TransactionalSession,
    ) {
        originalSvar.filterNot { (id, faktum) -> nyeSvar[id]?.svar() == faktum?.svar() }
            .forEach { (id, originaltFaktum) ->
                val (rootId, indeks) = fakta.id(id).reflection { rootId, indeks -> rootId to indeks }

                if (originaltFaktum?.erBesvart() == true) {
                    transactionalSession.run(arkiverFaktum(fakta, rootId, indeks))
                }
                transactionalSession.run(oppdaterFaktum(nyeSvar[id], fakta, indeks, rootId)).also {
                    require(it > 0) { "Fant ikke faktum som skal oppdateres i faktum_verdi, for faktumId=$id, root_id=$rootId, indeks=$indeks, søknadId=${fakta.uuid}" }
                }
            }
    }

    private fun svarMap(fakta: Fakta): MutableMap<String, Faktum<*>?> = fakta.associate { faktum ->
        faktum.id to (if (faktum.erBesvart()) faktum else null)
    }.toMutableMap()

    private fun slettDødeTemplatefakta(
        fakta: Fakta,
        nyeSvar: Map<String, Faktum<*>?>,
        originalSvar: MutableMap<String, Faktum<*>?>,
        transactionalSession: TransactionalSession,
    ) {
        originalSvar.keys.toSet().subtract(nyeSvar.keys.toSet())
            .map { FaktumId(it).reflection { rootId, indeks -> Triple(rootId, indeks, it) } }
            .forEach { (rootId, indeks, id) ->
                val originaltFaktum = originalSvar[id]?.svar()
                if (originaltFaktum != null) {
                    transactionalSession.run(arkiverFaktum(fakta = fakta, rootId = rootId, indeks = indeks))
                }
                transactionalSession.run(slettDødeFaktum(fakta = fakta, rootId, indeks))
                originalSvar.remove(id)
            }
    }

    private fun slettDødeFaktum(fakta: Fakta, rootId: Int, indeks: Int) = queryOf(
        //language=PostgreSQL
        """
              DELETE FROM faktum_verdi
              WHERE id IN 
                (SELECT faktum_verdi.id AS faktum_id FROM soknad, faktum_verdi, faktum
                  WHERE faktum_verdi.soknad_id = soknad.id 
                        AND faktum_verdi.faktum_id = faktum.id 
                        AND soknad.uuid = ? 
                        AND faktum.root_id = ? 
                        AND faktum_verdi.indeks = ?
                )
        """.trimIndent(),
        fakta.uuid,
        rootId,
        indeks,
    ).asExecute

    private fun svarList(uuid: UUID): List<FaktumVerdiRow> {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                        WITH soknad_faktum AS (SELECT faktum.id AS faktum_id, faktum.root_id AS root_id, soknad.id AS soknad_id FROM soknad, faktum
                                WHERE faktum.versjon_id = soknad.versjon_id AND faktum.regel IS NULL AND soknad.uuid = ?)
                            SELECT 
                                soknad_faktum.root_id AS root_id,
                                faktum_verdi.indeks AS indeks,
                                faktum_verdi.heltall AS heltall, 
                                faktum_verdi.desimaltall AS desimaltall, 
                                faktum_verdi.boolsk AS boolsk, 
                                faktum_verdi.dato AS dato, 
                                faktum_verdi.aarlig_inntekt AS aarlig_inntekt, 
                                besvarer.identifikator AS besvartav,
                                dokument.urn AS urn, 
                                dokument.opplastet AS opplastet,
                                envalg.verdier AS envalgverdier,
                                flervalg.verdier AS flervalgverdier,
                                faktum_verdi.tekst AS tekst,
                                faktum_verdi.land AS land,
                                periode.fom AS fom,
                                periode.tom AS tom
                            FROM faktum_verdi
                            JOIN soknad_faktum ON faktum_verdi.soknad_id = soknad_faktum.soknad_id 
                                AND faktum_verdi.faktum_id = soknad_faktum.faktum_id
                            LEFT JOIN dokument ON faktum_verdi.dokument_id = dokument.id
                            LEFT JOIN periode ON faktum_verdi.periode_id = periode.id
                            LEFT JOIN valgte_verdier envalg ON faktum_verdi.envalg_id = envalg.id
                            LEFT JOIN valgte_verdier flervalg ON faktum_verdi.flervalg_id = flervalg.id
                            LEFT JOIN besvarer ON faktum_verdi.besvart_av = besvarer.id
                            ORDER BY indeks""",
                    uuid,
                ).map {
                    FaktumVerdiRow(
                        it.int("root_id"),
                        it.int("indeks"),
                        it.intOrNull("heltall"),
                        it.anyOrNull("boolsk") as Boolean?,
                        it.underlying.getObject("dato", LocalDate::class.java),
                        it.doubleOrNull("aarlig_inntekt")?.årlig,
                        it.stringOrNull("urn"),
                        it.localDateTimeOrNull("opplastet"),
                        it.stringOrNull("besvartAv"),
                        it.doubleOrNull("desimaltall"),
                        it.arrayOrNull<String>("envalgVerdier")?.let { verdier -> Envalg(*verdier) },
                        it.arrayOrNull<String>("flervalgVerdier")?.let { verdier -> Flervalg(*verdier) },
                        it.stringOrNull("tekst")?.let { verdi -> Tekst(verdi) },
                        it.stringOrNull("land")?.let { verdi -> Land(verdi) },
                        it.localDateOrNull("fom"),
                        it.localDateOrNull("tom"),
                    )
                }.asList,
            )
        }
    }

    private class FaktumVerdiRow(
        val root_id: Int,
        val indeks: Int,
        val heltall: Int?,
        val boolsk: Boolean?,
        val dato: LocalDate?,
        val inntekt: Inntekt?,
        val urn: String?,
        val opplastet: LocalDateTime?,
        val besvartAv: String?,
        val desimaltall: Double?,
        val envalg: Envalg?,
        val flervalg: Flervalg?,
        val tekst: Tekst?,
        val land: Land?,
        val fom: LocalDate?,
        val tom: LocalDate?,
        val id: FaktumId = if (indeks == 0) FaktumId(root_id) else FaktumId(root_id).medIndeks(indeks),
    )

    @Suppress("UNCHECKED_CAST")
    private fun rehydrerFaktum(row: FaktumVerdiRow, faktum: Faktum<*>) {
        if (row.heltall != null) {
            (faktum as Faktum<Int>).rehydrer(row.heltall, row.besvartAv)
        }
        if (row.boolsk != null) {
            (faktum as Faktum<Boolean>).rehydrer(row.boolsk, row.besvartAv)
        }
        if (row.dato != null) {
            (faktum as Faktum<LocalDate>).rehydrer(row.dato, row.besvartAv)
        }
        if (row.inntekt != null) {
            (faktum as Faktum<Inntekt>).rehydrer(row.inntekt, row.besvartAv)
        }
        if (row.desimaltall != null) {
            (faktum as Faktum<Double>).rehydrer(row.desimaltall, row.besvartAv)
        }
        if (row.tekst != null) {
            (faktum as Faktum<Tekst>).rehydrer(row.tekst, row.besvartAv)
        }

        if (row.land != null) {
            (faktum as Faktum<Land>).rehydrer(row.land, row.besvartAv)
        }

        if (row.opplastet != null && row.urn != null) {
            (faktum as Faktum<Dokument>).rehydrer(
                Dokument(
                    row.opplastet,
                    row.urn,
                ),
                row.besvartAv,
            )
        }
        if (row.envalg != null) {
            (faktum as Faktum<Envalg>).rehydrer(row.envalg, row.besvartAv)
        }
        if (row.flervalg != null) {
            (faktum as Faktum<Flervalg>).rehydrer(row.flervalg, row.besvartAv)
        }
        if (row.fom != null) {
            (faktum as Faktum<Periode>).rehydrer(
                Periode(
                    row.fom,
                    row.tom,
                ),
                row.besvartAv,
            )
        }
    }

    private fun oppdaterFaktum(faktum: Faktum<*>?, fakta: Fakta, indeks: Int, rootId: Int): UpdateQueryAction =
        FaktumUpdateBuilder(fakta, indeks, rootId).build(faktum?.svar(), besvart(faktum?.besvartAv()))

    private fun besvart(besvartAv: String?): Int? {
        val besvarer = besvartAv ?: return null
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                         SELECT id FROM besvarer WHERE identifikator = :besvarer""",
                    mapOf("besvarer" to besvarer),
                ).map { it.int(1) }.asSingle,
            ) ?: session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    INSERT INTO besvarer (identifikator) VALUES (:besvarer) RETURNING id
                    """.trimIndent(),
                    mapOf("besvarer" to besvarer),
                ).map { it.int(1) }.asSingle,
            )
        }
    }

    private fun arkiverFaktum(fakta: Fakta, rootId: Int, indeks: Int): ExecuteQueryAction =
        queryOf(
            //language=PostgreSQL
            """INSERT INTO gammel_faktum_verdi (soknad_id, faktum_id, indeks, boolsk, aarlig_inntekt, dokument_id, dato, heltall, envalg_id, flervalg_id, tekst,land, periode_id, opprettet, besvart_av, desimaltall)
            SELECT soknad_id,
                   faktum_verdi.faktum_id,
                   faktum_verdi.indeks,
                   faktum_verdi.boolsk,
                   faktum_verdi.aarlig_inntekt,
                   faktum_verdi.dokument_id,
                   faktum_verdi.dato,
                   faktum_verdi.heltall,
                   faktum_verdi.envalg_id,
                   faktum_verdi.flervalg_id,
                   faktum_verdi.tekst,
                   faktum_verdi.land,
                   faktum_verdi.periode_id,
                   faktum_verdi.opprettet,
                   faktum_verdi.besvart_av,
                   faktum_verdi.desimaltall
            FROM faktum_verdi,
                 faktum,
                 soknad
            WHERE faktum_verdi.faktum_id = faktum.id
              AND faktum_verdi.soknad_id = soknad.id
              AND soknad.uuid = ?
              AND faktum.root_id = ?
              AND faktum_verdi.indeks = ?
            """.trimMargin(),
            fakta.uuid,
            rootId,
            indeks,
        ).asExecute

    private fun opprettTemplateFaktum(indeks: Int, fakta: Fakta, rootId: Int): ExecuteQueryAction =
        queryOf(
            //language=PostgreSQL
            """INSERT INTO faktum_verdi (indeks, soknad_id, faktum_id)
            SELECT :indeks, soknad.id, faktum.id
            FROM soknad,
                 faktum
            WHERE soknad.uuid = :soknadUuid
              AND faktum.versjon_id = soknad.versjon_id
              AND faktum.root_id = :rootId
            """.trimMargin(),
            mapOf(
                "indeks" to indeks,
                "soknadUuid" to fakta.uuid,
                "rootId" to rootId,
            ),
        ).asExecute

    override fun opprettede(identer: Identer): Map<LocalDateTime, UUID> {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    "SELECT opprettet, uuid FROM soknad WHERE person_id = (SELECT person_id FROM folkeregisterident WHERE verdi = ?)",
                    identer.first { it.type == Ident.Type.FOLKEREGISTERIDENT && !it.historisk }.id,
                ).map { it.localDateTime(1) to UUID.fromString(it.string(2)) }.asList,
            )
        }.toMap()
    }
}
