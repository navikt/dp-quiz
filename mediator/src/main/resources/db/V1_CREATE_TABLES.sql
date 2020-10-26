CREATE TABLE FAKTUM_VERDI
(
    id             BIGSERIAL,
    faktum_id      BIGSERIAL                not null references FAKTUM (id),
    faktum_type    INT                      NOT NULL,
    ja_nei         bool                     NULL,
    aarlig_inntekt decimal                  null,
    dokument_id    BIGSERIAL                null references DOKUMENT (ID),
    dato           TIMESTAMP WITH TIME ZONE NULL,
    heltall        int                      null,
    opprettet      TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE DOKUMENT
(
    id        BIGSERIAL                NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc'),
    url       varchar(256)             NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE FAKTA
(
    id         BIGSERIAL                NOT NULL,
    uuid       uuid                     NOT NULL,
    versjon_id int                      not null,
    fnr        char(11)                 not null,
    opprettet  TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc'),
    PRIMARY KEY (id)
);

create table FAKTA_FAKTUM
(
    fakta_id  BIGSERIAL not null references FAKTA (id),
    faktum_id BIGSERIAL not null references FAKTUM (id),
    primary key (fakta_id, faktum_id)
);

CREATE TABLE FAKTUM
(
    id              BIGSERIAL,
    root_id         int                      not null,
    indeks          int                      not null,
    navn_id         BIGSERIAL                not null references NAVN (id),
    faktum_verdi_id BIGSERIAL                not null references FAKTUM_VERDI (id),
    opprettet       TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc'),
    PRIMARY KEY (id)
);

CREATE TABLE NAVN
(
    id        BIGSERIAL                NOT NULL,
    navn      VARCHAR(256)             not null,
    opprettet TIMESTAMP WITH TIME ZONE NOT NULL default (now() at time zone 'utc'),
    PRIMARY KEY (id)
);
