-- ${flyway:timestamp}
DO
$$
    BEGIN
        IF EXISTS
            (SELECT 1 FROM pg_roles WHERE rolname = 'cloudsqliamuser')
        THEN
            REVOKE ALL ON ALL TABLES IN SCHEMA public FROM cloudsqliamuser;
            GRANT SELECT ON ALL TABLES IN SCHEMA public TO cloudsqliamuser;
        END IF;
    END
$$;