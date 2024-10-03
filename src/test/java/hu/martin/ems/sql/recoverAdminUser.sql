DELETE FROM loginuser WHERE id = 1;
INSERT INTO User (deleted, username, password, role_role_id) VALUES
    ('0', 'admin', 'admin', (SELECT id as role_role_id FROM Role WHERE id = 1 LIMIT 1))