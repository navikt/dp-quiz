CREATE TABLE IF NOT EXISTS V1_PROSESSVERSJON
(
    id         BIGSERIAL    NOT NULL,
    navn       varchar(256) NOT NULL,
    versjon_id int          NOT NULL,
    PRIMARY KEY (navn, versjon_id)
);

