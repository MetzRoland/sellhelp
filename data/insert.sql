INSERT INTO "user_roles" ("id", "role_name")
VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_MODERATOR'),
(3, 'ROLE_USER');

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