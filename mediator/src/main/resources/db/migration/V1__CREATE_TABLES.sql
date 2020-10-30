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
    url       VARCHAR(256)             NOT NULL,
    opplastet TIMESTAMP WITH TIME ZONE NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS faktum
(
    id          BIGSERIAL,
    versjon_id  INT                      NOT NULL,
    faktum_type INT                      NOT NULL,
    root_id     INT                      NOT NULL,
    navn_id     BIGSERIAL                NOT NULL REFERENCES navn (id),
    regel       VARCHAR(16)              NULL,
    opprettet   TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS soknad
(
    id         BIGSERIAL                NOT NULL,
    uuid       UUID                     NOT NULL,
    versjon_id INT                      NOT NULL,
    fnr        CHAR(11)                 NOT NULL,
    opprettet  TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS utledet_faktum
(
    parent_id BIGSERIAL NOT NULL REFERENCES faktum (id),
    child_id  BIGSERIAL NOT NULL REFERENCES faktum (id),
    PRIMARY KEY (parent_id, child_id)
);

CREATE TABLE IF NOT EXISTS template_faktum
(
    parent_id BIGSERIAL NOT NULL REFERENCES faktum (id),
    child_id  BIGSERIAL NOT NULL REFERENCES faktum (id),
    PRIMARY KEY (parent_id, child_id)
);

CREATE TABLE IF NOT EXISTS avhengig_faktum
(
    parent_id BIGSERIAL NOT NULL REFERENCES faktum (id),
    child_id  BIGSERIAL NOT NULL REFERENCES faktum (id),
    PRIMARY KEY (parent_id, child_id)
);

CREATE TABLE IF NOT EXISTS faktum_verdi
(
    id             BIGSERIAL,
    soknad_id      BIGSERIAL                NOT NULL REFERENCES soknad (id),
    faktum_id      BIGSERIAL                NOT NULL REFERENCES faktum (id),
    indeks         INT                      NOT NULL,
    ja_nei         BOOL                     NULL,
    aarlig_inntekt DECIMAL                  NULL,
    dokument_id    BIGINT                   NULL REFERENCES dokument (id),
    dato           DATE                     NULL,
    heltall        INT                      NULL,
    opprettet      TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS gammel_faktum_verdi
(
    id             BIGSERIAL,
    soknad_id      BIGSERIAL                NOT NULL REFERENCES soknad (id),
    faktum_id      BIGSERIAL                NOT NULL REFERENCES faktum (id),
    indeks         INT                      NOT NULL,
    ja_nei         BOOL                     NULL,
    aarlig_inntekt DECIMAL                  NULL,
    dokument_id    BIGINT                   NULL REFERENCES dokument (id),
    dato           DATE                     NULL,
    heltall        INT                      NULL,
    opprettet      TIMESTAMP WITH TIME ZONE NOT NULL,
    PRIMARY KEY (id)
);