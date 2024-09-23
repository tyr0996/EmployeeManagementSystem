INSERT INTO Address (deleted, countryCode_codestore_id, city_city_id, streetName, houseNumber, streetType_codestore_id) VALUES
	('0', (SELECT id as countryCode_codestore_id FROM CodeStore WHERE linkName ILIKE 'Hungary' LIMIT 1), (SELECT id as city_city_id FROM City WHERE name ILIKE 'Markotabödöge' LIMIT 1), 'Kossuth Lajos', '2', (SELECT id as streetType_codestore_id FROM CodeStore WHERE name ILIKE 'Street' LIMIT 1)),
	('0', (SELECT id as countryCode_codestore_id FROM CodeStore WHERE linkName ILIKE 'Hungary' LIMIT 1), (SELECT id as city_city_id FROM City WHERE name ILIKE 'Budapest' LIMIT 1), 'Szabadság', '64/b', (SELECT id as streetType_codestore_id FROM CodeStore WHERE name ILIKE 'Street' LIMIT 1))