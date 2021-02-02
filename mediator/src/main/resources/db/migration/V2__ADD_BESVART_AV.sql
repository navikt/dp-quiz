-- noinspection SqlNoDataSourceInspectionForFile

ALTER TABLE faktum_verdi ADD COLUMN IF NOT EXISTS besvart_av VARCHAR(20) DEFAULT NULL;

ALTER TABLE gammel_faktum_verdi ADD COLUMN IF NOT EXISTS besvart_av VARCHAR(20) DEFAULT NULL;