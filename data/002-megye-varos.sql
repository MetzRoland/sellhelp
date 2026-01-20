\copy counties(county_name) FROM '/docker-entrypoint-initdb.d/csakmegyek.csv' DELIMITER ',' CSV HEADER;


\copy cities(county_id, city_name) FROM '/docker-entrypoint-initdb.d/varos_megye_elol.csv' DELIMITER ',' CSV HEADER;
