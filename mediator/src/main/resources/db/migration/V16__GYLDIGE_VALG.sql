CREATE TABLE IF NOT EXISTS faktum_gyldige_valg
(
    faktum_id BIGSERIAL    NOT NULL REFERENCES faktum (id),
    valg      TEXT NOT NULL,
    PRIMARY KEY (faktum_id, valg)
);


CREATE TABLE IF NOT EXISTS valgt_verdi
(
    id   BIGSERIAL NOT NULL,
    verdier TEXT[] NOT NULL,
    PRIMARY KEY (id)
);


ALTER TABLE faktum_verdi ADD COLUMN IF NOT EXISTS envalg_id  BIGINT NULL REFERENCES valgt_verdi (id);
ALTER TABLE gammel_faktum_verdi ADD COLUMN IF NOT EXISTS envalg_id BIGINT NULL REFERENCES valgt_verdi (id);