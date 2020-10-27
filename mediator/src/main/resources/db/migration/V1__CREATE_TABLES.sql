-- noinspection SqlNoDataSourceInspectionForFile

CREATE TABLE IF NOT EXISTS navn
(
    id        BIGSERIAL                NOT NULL,
    navn      VARCHAR(256)             NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS dokument
(
    id        BIGSERIAL                NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    url       varchar(256)             NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS faktum
(
    id          BIGSERIAL,
    versjon_id  int                      NOT NULL,
    faktum_type INT                      NOT NULL,
    root_id     int                      NOT NULL,
    indeks      int                      NOT NULL,
    navn_id     BIGSERIAL                NOT NULL REFERENCES navn (id),
    opprettet   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS faktum_verdi
(
    id             BIGSERIAL,
    faktum_id      BIGSERIAL                NOT NULL REFERENCES faktum (id),
    ja_nei         bool                     NULL,
    aarlig_inntekt decimal                  NULL,
    dokument_id    BIGSERIAL REFERENCES dokument (ID),
    dato           TIMESTAMP WITH TIME ZONE NULL,
    heltall        int                      NULL,
    opprettet      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS fakta
(
    id         BIGSERIAL                NOT NULL,
    uuid       uuid                     NOT NULL,
    versjon_id int                      NOT NULL,
    fnr        char(11)                 NOT NULL,
    opprettet  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS fakta_faktum
(
    fakta_id  BIGSERIAL NOT NULL REFERENCES fakta (id),
    faktum_id BIGSERIAL NOT NULL REFERENCES faktum (id),
    PRIMARY KEY (fakta_id, faktum_id)
);

CREATE TABLE IF NOT EXISTS utledet_faktum
(
    parent_id  BIGSERIAL NOT NULL REFERENCES faktum (id),
    child_id BIGSERIAL NOT NULL REFERENCES faktum (id),
    PRIMARY KEY (parent_id, child_id)
);

CREATE TABLE IF NOT EXISTS template_faktum
(
    parent_id  BIGSERIAL NOT NULL REFERENCES faktum (id),
    child_id BIGSERIAL NOT NULL REFERENCES faktum (id),
    PRIMARY KEY (parent_id, child_id)
);

CREATE TABLE IF NOT EXISTS AVHENGIG_FAKTUM
(
    parent_id  BIGSERIAL NOT NULL REFERENCES faktum (id),
    child_id BIGSERIAL NOT NULL REFERENCES faktum (id),
    PRIMARY KEY (parent_id, child_id)
);


