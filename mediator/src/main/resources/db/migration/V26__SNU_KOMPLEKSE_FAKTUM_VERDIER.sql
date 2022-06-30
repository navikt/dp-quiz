--- DOKUMENT

ALTER TABLE dokument ADD COLUMN soknad_id BIGINT;

UPDATE dokument AS d
SET soknad_id = fv.soknad_id
FROM faktum_verdi AS fv
WHERE fv.dokument_id = d.id;


UPDATE dokument AS d
SET soknad_id = fv.soknad_id
FROM gammel_faktum_verdi AS fv
WHERE fv.dokument_id = d.id;


ALTER TABLE dokument
    ALTER COLUMN soknad_id SET NOT NULL,
    ADD CONSTRAINT soknad_id_fk FOREIGN KEY (soknad_id) REFERENCES soknad(id) ON DELETE CASCADE;

--- VALGTE VERDIER

ALTER TABLE valgte_verdier ADD COLUMN soknad_id BIGINT;

UPDATE valgte_verdier AS vv
SET soknad_id = fv.soknad_id
FROM faktum_verdi AS fv
WHERE vv.id = fv.envalg_id;


UPDATE valgte_verdier AS vv
SET soknad_id = fv.soknad_id
FROM gammel_faktum_verdi AS fv
WHERE vv.id = fv.envalg_id;


UPDATE valgte_verdier AS vv
SET soknad_id = fv.soknad_id
FROM faktum_verdi AS fv
WHERE vv.id = fv.flervalg_id;


UPDATE valgte_verdier AS vv
SET soknad_id = fv.soknad_id
FROM gammel_faktum_verdi AS fv
WHERE vv.id = fv.flervalg_id;


ALTER TABLE valgte_verdier
    ALTER COLUMN soknad_id SET NOT NULL,
    ADD CONSTRAINT faktum_verdi_id_fk FOREIGN KEY (soknad_id) REFERENCES soknad(id) ON DELETE CASCADE;


--- PERIODE

ALTER TABLE periode ADD COLUMN soknad_id BIGINT;

UPDATE periode AS p
SET soknad_id = fv.soknad_id
FROM faktum_verdi AS fv
WHERE fv.periode_id = p.id;



UPDATE periode AS p
SET soknad_id = fv.soknad_id
FROM gammel_faktum_verdi AS fv
WHERE fv.periode_id = p.id;


ALTER TABLE periode
    ALTER COLUMN soknad_id SET NOT NULL,
    ADD CONSTRAINT faktum_verdi_id_fk FOREIGN KEY (soknad_id) REFERENCES soknad(id) ON DELETE CASCADE;


--- Fjerne delete cascade fra "komplekse verdier" introdusert i forrige skript


ALTER TABLE faktum_verdi
    DROP CONSTRAINT faktum_verdi_dokument_id_fkey,
    ADD CONSTRAINT faktum_verdi_dokument_id_fkey FOREIGN KEY (dokument_id) REFERENCES dokument (id);

ALTER TABLE gammel_faktum_verdi
    DROP CONSTRAINT gammel_faktum_verdi_dokument_id_fkey,
    ADD CONSTRAINT gammel_faktum_verdi_dokument_id_fkey FOREIGN KEY (dokument_id) REFERENCES dokument (id);

ALTER TABLE faktum_verdi
    DROP CONSTRAINT faktum_verdi_envalg_id_fkey,
    ADD CONSTRAINT faktum_verdi_envalg_id_fkey FOREIGN KEY (envalg_id) REFERENCES valgte_verdier(id);

ALTER TABLE gammel_faktum_verdi
    DROP CONSTRAINT gammel_faktum_verdi_envalg_id_fkey,
    ADD CONSTRAINT gammel_faktum_verdi_envalg_id_fkey FOREIGN KEY (envalg_id) REFERENCES valgte_verdier(id);

ALTER TABLE faktum_verdi
    DROP CONSTRAINT faktum_verdi_flervalg_id_fkey,
    ADD CONSTRAINT faktum_verdi_flervalg_id_fkey FOREIGN KEY (flervalg_id) REFERENCES valgte_verdier(id);

ALTER TABLE gammel_faktum_verdi
    DROP CONSTRAINT gammel_faktum_verdi_flervalg_id_fkey,
    ADD CONSTRAINT gammel_faktum_verdi_flervalg_id_fkey FOREIGN KEY (flervalg_id) REFERENCES valgte_verdier(id);


ALTER TABLE faktum_verdi
    DROP CONSTRAINT faktum_verdi_periode_id_fkey,
    ADD CONSTRAINT faktum_verdi_periode_id_fkey FOREIGN KEY (periode_id) REFERENCES periode(id);

ALTER TABLE gammel_faktum_verdi
    DROP CONSTRAINT gammel_faktum_verdi_periode_id_fkey,
    ADD CONSTRAINT gammel_faktum_verdi_periode_id_fkey FOREIGN KEY (periode_id) REFERENCES periode(id);

