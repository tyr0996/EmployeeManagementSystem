INSERT INTO Supplier (deleted, name, address_address_id) VALUES
	('0', 'Szállító1', (SELECT id as address_address_id FROM Address WHERE id = 1 LIMIT 1)),
	('0', 'Szállító2', (SELECT id as address_address_id FROM Address WHERE id = 2 LIMIT 1))