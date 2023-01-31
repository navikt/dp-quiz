DROP PROCEDURE IF EXISTS slett_soknad;

ALTER TABLE soknad DROP COLUMN sesjon_type_id;
DROP TABLE sesjon_type