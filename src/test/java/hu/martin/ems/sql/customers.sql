INSERT INTO Customer (deleted, firstName, lastName, emailAddress, address_address_id) VALUES
	('0', 'Erdei', 'Róbert', 'robert.emailcime@gmail.com', (SELECT id as address_address_id FROM Address WHERE id = 1.0 LIMIT 1)),
	('0', 'Gálber', 'Martin', 'martin.emailcime@gmail.com', (SELECT id as address_address_id FROM Address WHERE id = 2.0 LIMIT 1))