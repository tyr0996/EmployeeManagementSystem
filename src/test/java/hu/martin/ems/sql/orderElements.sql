INSERT INTO OrderElement (deleted, product_product_id, customer_customer_id, supplier_supplier_id, unit, unitNetPrice, taxKey_codestore_id, netPrice, taxPrice, grossPrice) VALUES
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product1' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, '10', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '100', '27', '127'),
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product2' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, '20', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '200', '27', '254'),
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product2' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, '30', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '300', '27', '381'),
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product1' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, '50', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '500', '27', '635'),
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product1' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, '40', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '400', '27', '508'),
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product1' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, '80', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '800', '27', '1016'),
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product1' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 2.0 LIMIT 1), NULL, '90', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '900', '27', '1143'),
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product1' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 1.0 LIMIT 1), NULL, '10', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '100', '27', '127'),
	('0', (SELECT id as product_product_id FROM Product WHERE name ILIKE 'Product1' LIMIT 1), (SELECT id as customer_customer_id FROM Customer WHERE id = 1.0 LIMIT 1), NULL, '15', '10', (SELECT id as taxKey_codestore_id FROM CodeStore WHERE name ILIKE '27' LIMIT 1), '150', '27', '190')