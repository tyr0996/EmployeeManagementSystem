INSERT INTO User (deleted, username, passwordHash, role_role_id, enabled) VALUES
	('0', 'admin', '$2a$12$21wsdBKKqiHILOElhmEhGe3R11QIlrXmA6xlY.CowoExz8rlxB9Bu', (SELECT id as role_role_id FROM Role WHERE id = 1.0 LIMIT 1), 'true'),
	('0', 'Erzsi', '$2a$12$XGHOnxr5AyfmOoIjKEEP7.JXIXZgNiB53uf2AhbpwdAFztqi8FqCy', (SELECT id as role_role_id FROM Role WHERE id = 1.0 LIMIT 1), 'false'),
	('0', 'robi', '$2a$12$ENKhjamGSnSXx81f0IRPQObhyEOccAbutpkjJRai0.dshqFRyFETy', (SELECT id as role_role_id FROM Role WHERE id = 2.0 LIMIT 1), 'true')