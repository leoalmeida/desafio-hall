INSERT INTO APP_USERs (email, name, profile, password, status, reset_token) VALUES
    ('user@email.com', 'Usuario user', 'USER', '$2a$10$KbdhjX3F4l8/ibWNr0TFzus1ROvxUc8iHYsybJIaAEJOIflETefXy', 'ativo', NULL),
    ('admin@email.com', 'João Admin', 'ADMIN', '$2a$10$KbdhjX3F4l8/ibWNr0TFzus1ROvxUc8iHYsybJIaAEJOIflETefXy', 'ativo', NULL),
ON CONFLICT (email) DO NOTHING;

INSERT INTO APPLICATION (
    id, name, owner_team, repo_url, created_at, updated_at
) VALUES
    (1, 'App 1', 'Team A', 'https://github.com/teamA/app1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (2, 'App 2', 'Team B', 'https://github.com/teamB/app2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (3, 'App 3', 'Team C', 'https://github.com/teamC/app3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (4, 'App 4', 'Team D', 'https://github.com/teamD/app4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    (5, 'App 5', 'Team E', 'https://github.com/teamE/app5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('APPLICATION_SEQ', COALESCE((SELECT MAX(id) FROM APPLICATION), 1), true);

INSERT INTO RELEASE (
    id, application_id, version, env, status, evidence_url, version_row, created_at
) VALUES
    (1, 1, '1.0.0', 'production', 'deployed', 'https://evidence.com/release1', 0, CURRENT_TIMESTAMP),
    (2, 1, '1.1.0', 'staging', 'pending', NULL, 0, CURRENT_TIMESTAMP),
    (3, 2, '2.0.0', 'production', 'failed', 'https://evidence.com/release3', 0, CURRENT_TIMESTAMP),
    (4, 3, '3.0.0', 'production', 'deployed', 'https://evidence.com/release4', 0, CURRENT_TIMESTAMP),
    (5, 4, '4.0.0', 'staging', 'pending', NULL, 0, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('RELEASE_SEQ', COALESCE((SELECT MAX(id) FROM RELEASE), 1), true);

INSERT INTO APPROVAL (
    id, release_id, approver_email, outcome, notes, timestamp
) VALUES
    (1, 1, 'approver1@example.com', 'approved', 'Looks good', CURRENT_TIMESTAMP),
    (2, 2, 'approver2@example.com', 'rejected', 'Needs changes', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('APPROVAL_SEQ', COALESCE((SELECT MAX(id) FROM APPROVAL), 1), true);

INSERT INTO AUDITLOG (
    id, actor, action, entity, entity_id, payload, timestamp
) VALUES
    (1, 'system', 'insert', 'APP_USER', 1, '{"email": "pendente@email.com"}', CURRENT_TIMESTAMP),
    (2, 'system', 'insert', 'APPLICATION', 1, '{"name": "App 1"}', CURRENT_TIMESTAMP),
    (3, 'system', 'insert', 'RELEASE', 1, '{"version": "1.0.0"}', CURRENT_TIMESTAMP),
    (4, 'system', 'insert', 'APPROVAL', 1, '{"outcome": "approved"}', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

SELECT setval('AUDITLOG_SEQ', COALESCE((SELECT MAX(id) FROM AUDITLOG), 1), true);