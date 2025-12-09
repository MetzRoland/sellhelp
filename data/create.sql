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
  "selected_user_id" INT,
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
      REFERENCES "cities"("id"),
  CONSTRAINT "FK_posts_selected_user_id"
    FOREIGN KEY ("selected_user_id")
      REFERENCES "users"("id")
      ON DELETE SET NULL
);

CREATE TABLE "job_applications" (
  "id" INT,
  "user_id" INT,
  "post_id" INT,
  "applied_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),

  CONSTRAINT "FK_applications_user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "users"("id")
      ON DELETE CASCADE,

  CONSTRAINT "FK_applications_post_id"
    FOREIGN KEY ("post_id")
      REFERENCES "posts"("id")
      ON DELETE CASCADE
);

CREATE TABLE "job_applications" (
  "id" INT,
  "user_id" INT,
  "post_id" INT,
  "applied_at" TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY ("id"),

  CONSTRAINT "FK_applications_user_id"
    FOREIGN KEY ("user_id")
      REFERENCES "users"("id")
      ON DELETE CASCADE,

  CONSTRAINT "FK_applications_post_id"
    FOREIGN KEY ("post_id")
      REFERENCES "posts"("id")
      ON DELETE CASCADE
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
      ON DELETE SET NULL
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
      REFERENCES "users"("id")
      ON DELETE SET NULL,

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
      ON DELETE CASCADE
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
      ON DELETE CASCADE
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
      ON DELETE SET NULL,

  CONSTRAINT "FK_reviews_reviewed_user_id"
    FOREIGN KEY ("reviewed_user_id")
      REFERENCES "users"("id")
      ON DELETE CASCADE
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
      ON DELETE CASCADE
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
