CREATE TABLE IF NOT EXISTS periode
(
    id  BIGSERIAL NOT NULL,
    fom DATE NOT NULL,
    tom DATE NULL,
    PRIMARY KEY (id)
);


ALTER TABLE faktum_verdi ADD COLUMN IF NOT EXISTS periode_id  BIGINT NULL REFERENCES periode (id);
ALTER TABLE gammel_faktum_verdi ADD COLUMN IF NOT EXISTS periode_id BIGINT NULL REFERENCES periode (id);
