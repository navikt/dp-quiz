--- DOKUMENT

ALTER TABLE dokument ADD COLUMN faktum_verdi_id BIGINT;

UPDATE dokument
SET faktum_verdi_id = faktum_verdi.id
FROM faktum_verdi
WHERE faktum_verdi.dokument_id = dokument.id;

UPDATE dokument
SET faktum_verdi_id = gammel_faktum_verdi.id
FROM gammel_faktum_verdi
WHERE gammel_faktum_verdi.dokument_id = dokument.id;


ALTER TABLE dokument
    ALTER COLUMN faktum_verdi_id SET NOT NULL,
    ADD UNIQUE (faktum_verdi_id),
    ADD CONSTRAINT faktum_verdi_id_fk FOREIGN KEY (faktum_verdi_id) REFERENCES faktum_verdi(id) ON DELETE CASCADE;

--- VALGTE VERDIER

ALTER TABLE valgte_verdier ADD COLUMN faktum_verdi_id BIGINT;

UPDATE valgte_verdier
SET faktum_verdi_id = faktum_verdi.id
FROM faktum_verdi
WHERE faktum_verdi.envalg_id = valgte_verdier.id;


UPDATE valgte_verdier
SET faktum_verdi_id = faktum_verdi.id
FROM faktum_verdi
WHERE faktum_verdi.flervalg_id = valgte_verdier.id;

UPDATE valgte_verdier
SET faktum_verdi_id = gammel_faktum_verdi.id
FROM gammel_faktum_verdi
WHERE gammel_faktum_verdi.envalg_id = valgte_verdier.id;


UPDATE valgte_verdier
SET faktum_verdi_id = gammel_faktum_verdi.id
FROM gammel_faktum_verdi
WHERE gammel_faktum_verdi.flervalg_id = valgte_verdier.id;


ALTER TABLE valgte_verdier
    ALTER COLUMN faktum_verdi_id SET NOT NULL,
    ADD UNIQUE (faktum_verdi_id),
    ADD CONSTRAINT faktum_verdi_id_fk FOREIGN KEY (faktum_verdi_id) REFERENCES faktum_verdi(id) ON DELETE CASCADE;


--- PERIODE

ALTER TABLE periode ADD COLUMN faktum_verdi_id BIGINT;

UPDATE periode
SET faktum_verdi_id = faktum_verdi.id
FROM faktum_verdi
WHERE faktum_verdi.periode_id = periode.id;

UPDATE periode
SET faktum_verdi_id = gammel_faktum_verdi.id
FROM gammel_faktum_verdi
WHERE gammel_faktum_verdi.periode_id = periode.id;


ALTER TABLE periode
    ALTER COLUMN faktum_verdi_id SET NOT NULL,
    ADD UNIQUE (faktum_verdi_id),
    ADD CONSTRAINT faktum_verdi_id_fk FOREIGN KEY (faktum_verdi_id) REFERENCES faktum_verdi(id) ON DELETE CASCADE;






