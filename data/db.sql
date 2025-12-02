CREATE TABLE "counties" (
  "id" SERIAL,
  "county_name" VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY ("id")
);

CREATE TABLE "cities" (
  "id" SERIAL,
  "county_id" SMALLINT,
  "city_name" VARCHAR(100) NOT NULL UNIQUE,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_cities_county_id"
    FOREIGN KEY ("county_id")
      REFERENCES "counties"("id")
);

CREATE TABLE "post_status" (
  "id" SMALLINT,
  "status_name" VARCHAR(50) NOT NULL UNIQUE,
  PRIMARY KEY ("id")
);

CREATE TABLE "user_roles" (
  "id" SMALLINT,
  "role_name" VARCHAR(50) NOT NULL UNIQUE,
  PRIMARY KEY ("id")
);

CREATE TABLE "users" (
  "id" SERIAL,
  "username" VARCHAR(50) NOT NULL UNIQUE,
  "first_name" VARCHAR(50) NOT NULL,
  "last_name" VARCHAR(50) NOT NULL,
  "birth_date" DATE NOT NULL,
  "email" VARCHAR(50) NOT NULL UNIQUE,
  "profile_picture_path" VARCHAR(255),
  "city_id" SMALLINT NOT NULL,
  "google_id" VARCHAR(255),
  "role_id" SMALLINT NOT NULL,
  "created_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  "is_banned" BOOLEAN DEFAULT FALSE,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_users_role_id"
    FOREIGN KEY ("role_id")
      REFERENCES "user_roles"("id"),
  CONSTRAINT "FK_users_city_id"
    FOREIGN KEY ("city_id")
      REFERENCES "cities"("id")
);

CREATE TABLE "chats" (
  "id" SERIAL,
  "host_id" INT,
  "guest_id" INT,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_host_id"
    FOREIGN KEY ("host_id")
      REFERENCES "users"("id"),
  CONSTRAINT "FK_guest_id"
    FOREIGN KEY ("guest_id")
      REFERENCES "users"("id")
);

CREATE TABLE "posts" (
  "id" SERIAL,
  "user_id" INT,
  "title" VARCHAR(100) NOT NULL,
  "description" VARCHAR(2000) NOT NULL,
  "reward" INT DEFAULT 0,
  "status_id" SMALLINT,
  "city_id" INT,
  "created_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_posts_status_id"
    FOREIGN KEY ("status_id")
      REFERENCES "post_status"("id"),
  CONSTRAINT "FK_posts_user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "users"("id"),
  CONSTRAINT "FK_posts_city_id"
    FOREIGN KEY ("city_id")
      REFERENCES "cities"("id")
);

CREATE TABLE "comments" (
  "id" SERIAL,
  "post_id" INT,
  "user_id" INT,
  "message" VARCHAR(2000) NOT NULL,
  "created_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_post_id"
    FOREIGN KEY ("post_id")
      REFERENCES "posts"("id")
      ON DELETE CASCADE,
  CONSTRAINT "FK_user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "users"("id")
);


CREATE TABLE "chat_messages" (
  "id" SERIAL,
  "chat_id" INT,
  "user_id" INT,
  "message" VARCHAR(2000) NOT NULL,
  "sent_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_chat_messages_user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "users"("id"),
  CONSTRAINT "FK_chat_messages_chat_id"
    FOREIGN KEY ("chat_id")
      REFERENCES "chats"("id")
      ON DELETE CASCADE
);

CREATE TABLE "user_files" (
  "id" SERIAL,
  "user_id" INT,
  "file_path" VARCHAR(255) NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_user_files_user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "users"("id")
);

CREATE TABLE "chat_files" (
  "id" SERIAL,
  "message_id" INT,
  "file_path" VARCHAR(255) NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_chat_files_message_id"
    FOREIGN KEY ("message_id")
      REFERENCES "chat_messages"("id")
      ON DELETE CASCADE
);

CREATE TABLE "post_files" (
  "id" SERIAL,
  "post_id" INT,
  "file_path" VARCHAR(255) NOT NULL,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_post_files_post_id"
    FOREIGN KEY ("post_id")
      REFERENCES "posts"("id")
      ON DELETE CASCADE
);

CREATE TABLE "user_secrets" (
  "id" SERIAL,
  "user_id" INT,
  "password" CHAR(60) NOT NULL,
  "last_used_pass" CHAR(60),
  "pass_update_token" VARCHAR(60),
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_user_secrets_user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "users"("id")
);

CREATE TABLE "reviews" (
  "id" SERIAL,
  "sender_user_id" INT,
  "reviewed_user_id" INT,
  "rating" SMALLINT NOT NULL,
  "comment" VARCHAR(2000),
  "created_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_reviews_sender_user_id"
    FOREIGN KEY ("sender_user_id")
      REFERENCES "users"("id")
  CONSTRAINT "FK_reviews_reviewed_user_id"
    FOREIGN KEY ("reviewed_user_id")
      REFERENCES "users"("id")
);

CREATE TABLE "notifications" (
  "id" SERIAL,
  "user_id" INT,
  "title" VARCHAR(100) NOT NULL,
  "message" VARCHAR(2000) NOT NULL,
  "sent_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_notifications_user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "users"("id")
);

CREATE TABLE "reports" (
  "id" SERIAL,
  "reported_user_id" INT,
  "sender_user_id" INT,
  "report_type_id" SMALLINT,
  "created_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),
  CONSTRAINT "FK_reports_reported_user_id"
    FOREIGN KEY ("reported_user_id")
      REFERENCES "users"("id"),
  CONSTRAINT "FK_reports_sender_user_id"
    FOREIGN KEY ("sender_user_id")
      REFERENCES "users"("id")
);

CREATE TABLE "report_types" (
  "id" SMALLINT,
  "name" VARCHAR(30) NOT NULL UNIQUE,
  PRIMARY KEY ("id")
);

INSERT INTO "user_roles" ("id", "role_name")
VALUES 
(1, 'admin'),
(2, 'moderator'),
(3, 'user');

INSERT INTO "post_status" ("id", "status_name")
VALUES 
(1, 'new'),
(2, 'closed'),
(3, 'pending_closure'),
(4, 'available'),
(5, 'ended_with_conflict');

INSERT INTO "report_types" ("id", "name")
VALUES 
(1, 'scammer'),
(2, 'dangerous'),
(3, 'illegal_activity'),
(4, 'leaked_sensitive_data'),
(5, 'spam'),
(6, 'bot/not_a_real_person');

INSERT INTO "counties" ("county_name")
VALUES 
('Baranya');

INSERT INTO "cities" ("county_id", "city_name")
VALUES 
(1, 'Pécs');

INSERT INTO "users" (
  "username", "first_name", "last_name", "birth_date",
  "email", "city_id", "role_id"
)
VALUES (
  'johndoe', 'John', 'Doe', '1990-05-10',
  'john@example.com', 1, 3
);

INSERT INTO "user_secrets" ("user_id", "password")
VALUES (1, 'new_password');

UPDATE "user_secrets"
SET "password" = 'newer_password'
WHERE "user_id" = 1;

SELECT * FROM "user_secrets";

CREATE OR REPLACE FUNCTION update_last_used_pass()
RETURNS TRIGGER AS $$
BEGIN
    -- Check if password has changed
    IF NEW.password IS DISTINCT FROM OLD.password THEN
        NEW.last_used_pass := OLD.password;
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_update_last_used_pass
BEFORE UPDATE OF password ON "user_secrets"
FOR EACH ROW
EXECUTE FUNCTION update_last_used_pass();

CREATE INDEX idx_cities_city_name ON cities (city_name);

-- Enable FDW
CREATE EXTENSION IF NOT EXISTS postgres_fdw;

-- Enable pg_cron
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
GRANT ALL ON SCHEMA public TO public;
