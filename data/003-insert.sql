INSERT INTO "user_roles" ("id", "role_name")
VALUES 
(1, 'ROLE_ADMIN'),
(2, 'ROLE_MODERATOR'),
(3, 'ROLE_USER');

INSERT INTO "post_status" ("id", "status_name")
VALUES 
(1, 'new'),
(2, 'accepted'),
(3, 'started'),
(4, 'completed_by_employee'),
(5, 'unsuccessful_result_closed'),
(6, 'work_rejected'),
(7, 'closed');

INSERT INTO "report_types" ("id", "name")
VALUES 
(1, 'scammer'),
(2, 'dangerous'),
(3, 'illegal_activity'),
(4, 'leaked_sensitive_data'),
(5, 'spam'),
(6, 'bot/not_a_real_person');

INSERT INTO "users" (
  "first_name", "last_name", "birth_date",
  "email", "city_id", "auth_provider", "role_id"
)
VALUES
('Kovács', 'Péter', '1988-03-12', 'kovacs.peter@example.com', 1, 'LOCAL', 3),
('Nagy', 'Anna', '1992-07-25', 'nagy.anna@example.com', 2, 'LOCAL', 3),
('Szabó', 'Gábor', '1985-11-05', 'szabo.gabor@example.com', 3, 'LOCAL', 3),
('Tóth', 'Eszter', '1995-01-18', 'toth.eszter@example.com', 1, 'LOCAL', 3),
('Varga', 'László', '1990-09-30', 'varga.laszlo@example.com', 2, 'LOCAL', 3),
('Kiss', 'Réka', '1998-06-14', 'kiss.reka@example.com', 3, 'LOCAL', 3),
('Molnár', 'Balázs', '1983-12-22', 'molnar.balazs@example.com', 1, 'LOCAL', 3),
('Horváth', 'Zsófia', '1996-04-08', 'horvath.zsofia@example.com', 2, 'LOCAL', 3),
('Metz', 'Roland', '2005-02-12', 'metz.roland@example.com', 2128, 'LOCAL', 2),
('Ömböli', 'János', '2005-12-21', 'omboli.janos@example.com', 2128, 'LOCAL', 1);

-- Fiókok jelszava: KeresztnevVezeteknev1111.

INSERT INTO "user_secrets" ("user_id", "password", "last_used_pass", "is_mfa", "totp_secret") VALUES
(1,	'$2a$10$TdMsUuai2J60nh1fTUpzCORJOPhCdU/nQzdn6EdaFSp/kPP8LkPDq',	NULL,	'0',	NULL),
(2,	'$2a$10$hjdc.sdHzuzensofPTK/Uuo3Uiq4fw0tfpuHJe2OAQ4OcwIJn4qGu',	NULL,	'0',	NULL),
(3,	'$2a$10$WO9t1DbnrPP7GIBQYitNSufakeYQpeId/PZBiJUGcrrMEALiIRKem',	NULL,	'0',	NULL),
(4,	'$2a$10$.DE6T8SpL8ccJnYvNiJLUO4.6a.KTvkY5hy1lY4WMc.4t4X47TPXG',	NULL,	'0',	NULL),
(5,	'$2a$10$DcykPJl98JVmrddb6w0DAO5DIOaha.8wKa2abxx550YH0bgnfyo6C',	NULL,	'0',	NULL),
(6,	'$2a$10$ggaypeWFm6ksD5YBqw4OA.i75vyjgy/rfjVPUC3HPqxDvfS7Koq86',	NULL,	'0',	NULL),
(7,	'$2a$10$N3eXsphGTSvxe6jHkRtpb.eSVpGHdzCRXgO6O8uAK/SBLlpqiJPi.',	NULL,	'0',	NULL),
(8,	'$2a$10$99DG5UZzDnTP9blVxUukkuf.9txgarZy86F0poi3Yqc7BdVtyMAfe',	NULL,	'0',	NULL),
(9,	'$2a$10$JFU.YgC6ZWOs20gqpJTtZu9/MulTORShTDDIXEG3rXrs/Owztz9M6',	NULL,	'0',	NULL),
(10,'$2a$10$ZgaqVyzhZSf5CzX.CBGHp.cVvcTcZYexRzV6VLBtJCw793hydc3Hu',	NULL,	'0',	NULL);

INSERT INTO "posts" (
  "user_id", "title", "description", "reward", "status_id", "city_id"
)
VALUES
(1, 'Segítség költözéshez', 'Költözéshez keresek segítséget szombatra, dobozok pakolása és bútorok szállítása.', 10000, 1, 1),

(2, 'Számítógép javítás', 'A laptopom nem indul el, valószínűleg hardveres probléma. Szakértő segítséget keresek.', 8000, 1, 2),

(3, 'Bevásárlás idősnek', 'Idős rokon számára heti bevásárlás elvégzése szükséges.', 3000, 1, 3),

(4, 'Bútor összeszerelés', 'IKEA szekrény összeszereléséhez keresek valakit.', 5000, 1, 1),

(5, 'Kert rendbetétele', 'Kisebb kert rendbetétele, fűnyírás és gazolás.', 7000, 1, 2),

(6, 'Villanyszerelési munka', 'Konnektor cseréje és egy lámpa felszerelése szükséges.', 6000, 1, 1),

(7, 'Falra kép felrakása', 'Egy nagyobb kép felfúrásához keresek segítséget.', 2000, 1, 3),

(8, 'Weboldal készítés', 'Egyszerű bemutatkozó weboldalt szeretnék kisvállalkozás számára.', 20000, 1, 2),

(1, 'Takarítás', 'Lakás teljes kitakarítása költözés előtt.', 9000, 1, 1),

(2, 'Kerékpár javítás', 'A biciklim lánca leesik, javítást igényel.', 4000, 1, 3);

INSERT INTO posts (
  user_id, title, description, reward, status_id, city_id
) VALUES

-- Egyszerű, hétköznapi segítség
(3, 'Kutya sétáltatás', 'Heti 3 alkalommal kellene megsétáltatni egy közepes méretű kutyát.', 3000, 1, 3),

(4, 'Idős ember kísérése orvoshoz', 'Egy idős hölgyet kellene elkísérni rendelésre és vissza.', 4000, 1, 1),

(5, 'Csomag átvétele és kiszállítása', 'Egy csomagot kell átvenni és elvinni a városon belül.', 2500, 1, 2),

(6, 'Fűnyírás családi háznál', 'Kb. 500m²-es kert lenyírása, saját eszköz előny.', 6000, 1, 1),

-- Lakás körüli munkák
(7, 'Csap csöpögés megszüntetése', 'A konyhai csap csöpög, javítás szükséges.', 3500, 1, 2),

(8, 'Ajtó zár csere', 'Bejárati ajtó zárját kellene kicserélni.', 5000, 1, 3),

(1, 'Festés egy szobában', 'Kb. 20m²-es szoba kifestése fehérre.', 15000, 1, 1),

(2, 'Padló lerakás', 'Laminált padló lerakása egy kisebb szobában.', 18000, 1, 2),

-- IT / digitális munkák
(3, 'Excel segítség', 'Egyszerű táblázat készítésében lenne szükség segítségre.', 4000, 1, 3),

(4, 'WordPress oldal javítás', 'Egy meglévő weboldalon kisebb hibák javítása.', 12000, 1, 1),

(5, 'Logó tervezés', 'Egyszerű logót szeretnék egy induló vállalkozáshoz.', 10000, 1, 2),

(6, 'Social media kezelés', 'Facebook oldal heti 2-3 poszt kezelése.', 8000, 1, 3),

-- Speciálisabb munkák
(7, 'Autó átnézés vásárlás előtt', 'Használt autó átvizsgálásához keresek szakembert.', 7000, 1, 1),

(8, 'Gáztűzhely bekötése', 'Új gáztűzhely szakszerű bekötése szükséges.', 9000, 1, 2),

(1, 'Klíma tisztítás', 'Otthoni klíma karbantartása és tisztítása.', 8000, 1, 1),

-- Kreatív / extra
(2, 'Fotózás családi eseményen', 'Kisebb családi esemény fotózása 2-3 órában.', 15000, 1, 3),

(3, 'Zeneoktatás (gitár)', 'Kezdő gitár oktatás heti 1 alkalommal.', 5000, 1, 2),

(4, 'Gyerekfelügyelet', 'Pár órás gyerekfelügyelet hétvégén.', 6000, 1, 1),

-- Futár / gyors feladatok
(5, 'Gyógyszer kiváltása', 'Gyógyszert kellene kiváltani és elhozni.', 2000, 1, 2),

(6, 'Dokumentum leadása', 'Fontos papírt kellene elvinni egy irodába.', 1500, 1, 3);

INSERT INTO "comments" ("post_id", "user_id", "message", "created_at") VALUES
(1, 2, 'Szia! Tudok segíteni szombaton, van tapasztalatom költözésben.', '2026-03-30 18:00:00'),
(1, 3, 'Hány órára lenne szükség a segítségre?', '2026-03-30 18:05:00'),
(1, 5, 'Van nagyobb autóm is, ha kell szállításban is segítek.', '2026-03-30 18:10:00'),

(2, 8, 'Szia! IT-s vagyok, meg tudom nézni a laptopot.', '2026-03-30 18:20:00'),
(2, 6, 'Volt valami előjele a hibának?', '2026-03-30 18:25:00'),

(3, 4, 'Szia! Szívesen segítek heti rendszerességgel is.', '2026-03-30 18:30:00'),
(3, 7, 'Melyik boltba kellene menni?', '2026-03-30 18:35:00'),

(4, 1, 'Raktam már össze ilyen IKEA szekrényt, vállalom.', '2026-03-30 18:40:00'),
(4, 6, 'Mekkora a szekrény pontosan?', '2026-03-30 18:42:00'),

(5, 3, 'Van saját fűnyíróm, meg tudom oldani.', '2026-03-30 18:50:00'),
(5, 2, 'Kb mekkora a terület?', '2026-03-30 18:52:00'),

(6, 7, 'Villanyszerelő vagyok, szakszerűen megcsinálom.', '2026-03-30 19:00:00'),
(6, 5, 'Új konnektor is van már hozzá?', '2026-03-30 19:05:00'),

(7, 1, 'Fúróm van, gyorsan megoldjuk.', '2026-03-30 19:10:00'),
(7, 8, 'Beton fal vagy gipszkarton?', '2026-03-30 19:12:00'),

(8, 2, 'Frontend fejlesztő vagyok, tudok segíteni.', '2026-03-30 19:20:00'),
(8, 3, 'Milyen funkciókra lenne szükség pontosan?', '2026-03-30 19:22:00'),
(8, 7, 'Teljes weboldalt is vállalok backenddel együtt.', '2026-03-30 19:25:00'),

(9, 4, 'Szia! Van tapasztalatom nagytakarításban.', '2026-03-30 19:30:00'),
(9, 6, 'Hány négyzetméteres a lakás?', '2026-03-30 19:32:00'),

(10, 5, 'Szia! Valószínűleg lánc vagy váltó probléma, meg tudom javítani.', '2026-03-30 19:40:00'),
(10, 7, 'Ha kell, alkatrészt is hozok hozzá.', '2026-03-30 19:42:00');

INSERT INTO job_applications (user_id, post_id) VALUES
-- Post 1 (költözés)
(2, 1),
(3, 1),
(5, 1),

-- Post 2 (laptop javítás)
(8, 2),
(6, 2),

-- Post 3 (bevásárlás)
(4, 3),
(7, 3),

-- Post 4 (bútor)
(1, 4),
(6, 4),

-- Post 5 (kert)
(3, 5),
(2, 5),

-- Post 6 (villanyszerelés)
(7, 6),
(5, 6),

-- Post 7 (kép felfúrás)
(1, 7),
(8, 7),

-- Post 8 (weboldal)
(2, 8),
(3, 8),
(7, 8),

-- Post 9 (takarítás)
(4, 9),
(6, 9),

-- Post 10 (bicikli)
(5, 10),
(7, 10);

UPDATE posts SET selected_user_id = 2 WHERE id = 1;
UPDATE posts SET selected_user_id = 8 WHERE id = 2;
UPDATE posts SET selected_user_id = 4 WHERE id = 3;
UPDATE posts SET selected_user_id = 1 WHERE id = 4;
UPDATE posts SET selected_user_id = 3 WHERE id = 5;
UPDATE posts SET selected_user_id = 7 WHERE id = 6;
UPDATE posts SET selected_user_id = 1 WHERE id = 7;
UPDATE posts SET selected_user_id = 2 WHERE id = 8;
UPDATE posts SET selected_user_id = 4 WHERE id = 9;
UPDATE posts SET selected_user_id = 5 WHERE id = 10;

UPDATE posts
SET status_id = 2 -- accepted
WHERE selected_user_id IS NOT NULL;

UPDATE posts
SET status_id = 3 -- started
WHERE id IN (1, 2, 4, 6);

UPDATE posts
SET status_id = 4 -- completed_by_employee
WHERE id IN (1, 4);

UPDATE posts
SET status_id = 5 -- unsuccessful_result_closed
WHERE id IN (3);

UPDATE posts
SET status_id = 6 -- work_rejected
WHERE id IN (6);

UPDATE posts
SET status_id = 7 -- closed
WHERE id IN (1, 4, 3, 6);