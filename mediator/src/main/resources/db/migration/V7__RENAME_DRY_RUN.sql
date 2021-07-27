DROP TABLE dry_run;

CREATE TABLE IF NOT EXISTS saksbehandles_på_ekte
(
    id                    BIGSERIAL,
    soknad_id             BIGSERIAL                NOT NULL REFERENCES soknad (id),
    saksbehandles_på_ekte BOOLEAN                  NOT NULL,
    opprettet             TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
)
