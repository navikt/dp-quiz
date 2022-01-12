CREATE TABLE IF NOT EXISTS faktum_gyldige_valg
(
    faktum_id BIGSERIAL    NOT NULL REFERENCES faktum (id),
    verdier TEXT[] NOT NULL,
    PRIMARY KEY (faktum_id, verdier)
);


CREATE TABLE IF NOT EXISTS valgte_verdier
(
    id   BIGSERIAL NOT NULL,
    verdier TEXT[] NOT NULL,
    PRIMARY KEY (id)
);


ALTER TABLE faktum_verdi ADD COLUMN IF NOT EXISTS envalg_id  BIGINT NULL REFERENCES valgte_verdier (id);
ALTER TABLE faktum_verdi ADD COLUMN IF NOT EXISTS flervalg_id  BIGINT NULL REFERENCES valgte_verdier (id);
ALTER TABLE gammel_faktum_verdi ADD COLUMN IF NOT EXISTS envalg_id BIGINT NULL REFERENCES valgte_verdier (id);
ALTER TABLE gammel_faktum_verdi ADD COLUMN IF NOT EXISTS flervalg_id  BIGINT NULL REFERENCES valgte_verdier (id);