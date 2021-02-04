-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS besvarer(
  id BIGSERIAL NOT NULL PRIMARY KEY,
  identifikator VARCHAR(20) NOT NULL,
  opprettet TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc')
);

ALTER TABLE faktum_verdi ADD COLUMN IF NOT EXISTS besvart_av BIGSERIAL REFERENCES besvarer(id);

ALTER TABLE gammel_faktum_verdi ADD COLUMN IF NOT EXISTS besvart_av BIGSERIAL REFERENCES besvarer(id);

INSERT INTO besvarer(id, identifikator) VALUES (1, 'system') ON CONFLICT DO NOTHING;