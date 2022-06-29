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

