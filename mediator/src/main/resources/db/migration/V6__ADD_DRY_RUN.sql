CREATE TABLE IF NOT EXISTS dry_run
(
    id        BIGSERIAL,
    soknad_id BIGSERIAL                NOT NULL REFERENCES soknad (id),
    dry_run   BOOLEAN                  NOT NULL,
    opprettet TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT (NOW() AT TIME ZONE 'utc'),
    PRIMARY KEY (id)
)
