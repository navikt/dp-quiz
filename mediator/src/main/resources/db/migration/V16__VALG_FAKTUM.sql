CREATE TABLE IF NOT EXISTS faktum_valg
(
    faktum_id BIGSERIAL    NOT NULL REFERENCES faktum (id),
    valg      TEXT NOT NULL,
    PRIMARY KEY (faktum_id, valg)
);


CREATE TABLE IF NOT EXISTS valg
(
    id   BIGSERIAL NOT NULL,
    verdier TEXT[] NOT NULL,
    PRIMARY KEY (id)
);


ALTER TABLE faktum_verdi ADD COLUMN IF NOT EXISTS valg_id  BIGINT NULL REFERENCES valg (id);
ALTER TABLE gammel_faktum_verdi ADD COLUMN IF NOT EXISTS valg_id BIGINT NULL REFERENCES valg (id);