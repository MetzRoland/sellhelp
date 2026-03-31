CREATE OR REPLACE FUNCTION update_last_used_pass()
RETURNS TRIGGER AS $$
BEGIN
    IF NEW.password IS DISTINCT FROM OLD.password THEN
        NEW.last_used_pass := OLD.password;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_last_used_pass
BEFORE UPDATE ON "user_secrets"
FOR EACH ROW
EXECUTE FUNCTION update_last_used_pass();

-- CREATE INDEX idx_cities_city_name ON cities (city_name);

/* CREATE EXTENSION IF NOT EXISTS postgres_fdw;

CREATE EXTENSION IF NOT EXISTS pg_cron;

CREATE SERVER sellhelpdb_server
FOREIGN DATA WRAPPER postgres_fdw
OPTIONS (host 'sellhelp-database.cj2eg666q0kr.eu-north-1.rds.amazonaws.com', dbname 'sellhelp', port '5432');

CREATE USER MAPPING FOR sandbox
SERVER sellhelpdb_server
OPTIONS (user 'sandbox', password 'SandBoxPassword1111.');

IMPORT FOREIGN SCHEMA public
LIMIT TO (posts, post_files, post_status)
FROM SERVER sellhelpdb_server
INTO public;

DELETE FROM posts
WHERE created_at < NOW() - INTERVAL '30 days';

SELECT cron.schedule(
    'daily_delete_expired_posts',
    '0 0 * * *',
$$
DELETE FROM posts
WHERE created_at < NOW() - INTERVAL '30 days'
  AND status_id <> (
        SELECT id FROM post_status 
        WHERE status_name = 'closed'
      );
$$
);

DROP SERVER IF EXISTS sellhelpdb_server CASCADE;

DROP SCHEMA public CASCADE;
CREATE SCHEMA public;

GRANT ALL ON SCHEMA public TO sandbox;
GRANT ALL ON SCHEMA public TO public; */