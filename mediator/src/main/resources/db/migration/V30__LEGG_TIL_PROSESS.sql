ALTER TABLE IF EXISTS v1_prosessversjon
    RENAME TO faktaversjon;
ALTER TABLE IF EXISTS soknad
    RENAME TO fakta;

CREATE TABLE IF NOT EXISTS prosess
(
    id        BIGSERIAL                NOT NULL,
    uuid      uuid                     NOT NULL,
    navn      TEXT                     NOT NULL,
    person_id uuid                     NOT NULL REFERENCES person (uuid),
    fakta_id  uuid                     NOT NULL REFERENCES fakta (uuid),
    opprettet TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

INSERT INTO prosess (uuid, person_id, fakta_id, opprettet, navn)
SELECT uuid, person_id, uuid, opprettet, v1p.navn
FROM fakta f
         LEFT JOIN faktaversjon v1p ON f.versjon_id = v1p.id;

ALTER TABLE IF EXISTS fakta
    ALTER COLUMN sesjon_type_id DROP NOT NULL;