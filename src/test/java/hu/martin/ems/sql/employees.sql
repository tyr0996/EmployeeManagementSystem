INSERT INTO Employee (deleted, firstName, lastName, user_loginuser_id, salary) VALUES
	('0', 'Erdei', 'Róbert', (SELECT id as user_loginuser_id FROM loginuser WHERE id = 1.0 LIMIT 1), '10000'),
	('0', 'Gálber', 'Martin', (SELECT id as user_loginuser_id FROM loginuser WHERE id = 2.0 LIMIT 1), '10000'),
	('0', 'Kasszás', 'Erzsi', (SELECT id as user_loginuser_id FROM loginuser WHERE id = 3.0 LIMIT 1), '100')