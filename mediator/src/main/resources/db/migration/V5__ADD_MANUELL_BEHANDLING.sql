CREATE TABLE IF NOT EXISTS manuell_behandling
(
    id        BIGSERIAL,
    soknad_id BIGSERIAL                NOT NULL REFERENCES soknad (id),
    grunn     VARCHAR(256)             NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
)
