-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS besvarer
(
    id            BIGSERIAL PRIMARY KEY,
    identifikator VARCHAR(20)              NOT NULL UNIQUE,
    opprettet     TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

ALTER TABLE faktum_verdi ADD COLUMN IF NOT EXISTS besvart_av BIGINT NULL REFERENCES besvarer(id);

ALTER TABLE gammel_faktum_verdi ADD COLUMN IF NOT EXISTS besvart_av BIGINT NULL REFERENCES besvarer(id);

