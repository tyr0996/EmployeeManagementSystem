INSERT INTO Order (deleted, state_codestore_id, timeOfOrder, customer_customer_id, supplier_supplier_id, paymentType_codestore_id, currency_codestore_id) VALUES
	('0', (SELECT id as state_codestore_id FROM CodeStore WHERE name ILIKE 'Pending' LIMIT 1), '2024-06-19T09:10:55', (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, (SELECT id as paymentType_codestore_id FROM CodeStore WHERE name ILIKE 'Cash' LIMIT 1), (SELECT id as currency_codestore_id FROM CodeStore WHERE name ILIKE 'HUF' LIMIT 1)),
	('0', (SELECT id as state_codestore_id FROM CodeStore WHERE name ILIKE 'Pending' LIMIT 1), '2024-06-19T09:10:55', (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, (SELECT id as paymentType_codestore_id FROM CodeStore WHERE name ILIKE 'Cash' LIMIT 1), (SELECT id as currency_codestore_id FROM CodeStore WHERE name ILIKE 'HUF' LIMIT 1))