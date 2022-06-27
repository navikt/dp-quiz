CREATE OR REPLACE PROCEDURE slett_soknad(
    soknad_uuid UUID
)
    LANGUAGE plpgsql
AS
$$
DECLARE
    soknad_id_delete BIGINT;
BEGIN
    SELECT id FROM soknad WHERE uuid = soknad_uuid INTO soknad_id_delete;
    CREATE TEMP TABLE temp_besvarer ON COMMIT DROP AS SELECT besvart_av FROM faktum_verdi WHERE soknad_id = soknad_id_delete;
    DELETE FROM dokument WHERE id IN (SELECT dokument_id FROM faktum_verdi WHERE soknad_id = soknad_id_delete);
    DELETE FROM dokument WHERE id IN (SELECT dokument_id FROM gammel_faktum_verdi WHERE soknad_id = soknad_id_delete);
    DELETE FROM valgte_verdier WHERE id IN (SELECT envalg_id FROM faktum_verdi WHERE soknad_id = soknad_id_delete);
    DELETE FROM valgte_verdier WHERE id IN (SELECT envalg_id FROM gammel_faktum_verdi WHERE soknad_id = soknad_id_delete);
    DELETE FROM valgte_verdier WHERE id IN (SELECT flervalg_id FROM faktum_verdi WHERE soknad_id = soknad_id_delete);
    DELETE FROM valgte_verdier WHERE id IN (SELECT flervalg_id FROM gammel_faktum_verdi WHERE soknad_id = soknad_id_delete);
    DELETE FROM periode WHERE id IN (SELECT periode_id FROM faktum_verdi WHERE soknad_id = soknad_id_delete);
    DELETE FROM periode WHERE id IN (SELECT periode_id FROM gammel_faktum_verdi WHERE soknad_id = soknad_id_delete);
    DELETE FROM soknad WHERE id = soknad_id_delete;
    DELETE FROM besvarer WHERE id IN (SELECT besvart_av FROM temp_besvarer);
    COMMIT;
END;
$$