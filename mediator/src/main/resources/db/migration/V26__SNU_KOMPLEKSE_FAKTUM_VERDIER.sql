--- DOKUMENT

ALTER TABLE dokument ADD COLUMN soknad_id BIGINT;

UPDATE dokument
SET soknad_id = faktum_verdi.soknad_id
FROM faktum_verdi
WHERE faktum_verdi.dokument_id = dokument.id;

UPDATE dokument
SET soknad_id = gammel_faktum_verdi.soknad_id
FROM gammel_faktum_verdi
WHERE gammel_faktum_verdi.dokument_id = dokument.id;


ALTER TABLE dokument
    ALTER COLUMN soknad_id SET NOT NULL,
    ADD CONSTRAINT soknad_id_fk FOREIGN KEY (soknad_id) REFERENCES soknad(id) ON DELETE CASCADE;

--- VALGTE VERDIER

ALTER TABLE valgte_verdier ADD COLUMN soknad_id BIGINT;

UPDATE valgte_verdier
SET soknad_id = faktum_verdi.soknad_id
FROM faktum_verdi
WHERE faktum_verdi.envalg_id = valgte_verdier.id;


UPDATE valgte_verdier
SET soknad_id = faktum_verdi.soknad_id
FROM faktum_verdi
WHERE faktum_verdi.flervalg_id = valgte_verdier.id;

UPDATE valgte_verdier
SET soknad_id = gammel_faktum_verdi.soknad_id
FROM gammel_faktum_verdi
WHERE gammel_faktum_verdi.envalg_id = valgte_verdier.id;


UPDATE valgte_verdier
SET soknad_id = gammel_faktum_verdi.soknad_id
FROM gammel_faktum_verdi
WHERE gammel_faktum_verdi.flervalg_id = valgte_verdier.id;


ALTER TABLE valgte_verdier
    ALTER COLUMN soknad_id SET NOT NULL,
    ADD CONSTRAINT faktum_verdi_id_fk FOREIGN KEY (soknad_id) REFERENCES soknad(id) ON DELETE CASCADE;


--- PERIODE

ALTER TABLE periode ADD COLUMN soknad_id BIGINT;

UPDATE periode
SET soknad_id = faktum_verdi.soknad_id
FROM faktum_verdi
WHERE faktum_verdi.periode_id = periode.id;

UPDATE periode
SET soknad_id = gammel_faktum_verdi.soknad_id
FROM gammel_faktum_verdi
WHERE gammel_faktum_verdi.periode_id = periode.id;


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

