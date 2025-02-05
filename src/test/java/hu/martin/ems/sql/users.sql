INSERT INTO User (deleted, username, passwordHash, role_role_id, enabled) VALUES
	('0', 'admin', '$2a$12$Ei2ntwIK/6lePBO2UecedetPpxxDee3kmxnkWTXZI.CiPb86vejHe', (SELECT id as role_role_id FROM Role WHERE id = 1.0 LIMIT 1), 'true'),
	('0', 'robi', '$2a$12$nEBYNfVpfUZ9.qFFUGWs5.5mDLxXAGNlDcqKsxi7eoEFleys7Lvym', (SELECT id as role_role_id FROM Role WHERE id = 2.0 LIMIT 1), 'true')