INSERT INTO Employee (deleted, firstName, lastName, role_role_id, salary) VALUES
	('0', 'Erdei', 'Róbert', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1), '10000'),
	('0', 'Gálber', 'Martin', (SELECT id as role_role_id FROM Role WHERE id = 2 LIMIT 1), '10000')