ALTER TABLE faktum_verdi DROP CONSTRAINT faktum_verdi_soknad_id_fkey,
ADD CONSTRAINT faktum_verdi_soknad_id_fkey FOREIGN KEY (soknad_id) REFERENCES soknad(id) ON DELETE CASCADE;

ALTER TABLE gammel_faktum_verdi DROP CONSTRAINT gammel_faktum_verdi_soknad_id_fkey,
ADD CONSTRAINT gammel_faktum_verdi_soknad_id_fkey FOREIGN KEY (soknad_id) REFERENCES soknad(id) ON DELETE CASCADE;

ALTER TABLE faktum_verdi DROP CONSTRAINT faktum_verdi_dokument_id_fkey,
ADD CONSTRAINT faktum_verdi_dokument_id_fkey FOREIGN KEY (dokument_id) REFERENCES dokument(id) ON DELETE CASCADE;

ALTER TABLE gammel_faktum_verdi DROP CONSTRAINT gammel_faktum_verdi_dokument_id_fkey,
ADD CONSTRAINT gammel_faktum_verdi_dokument_id_fkey FOREIGN KEY (dokument_id) REFERENCES dokument(id) ON DELETE CASCADE;