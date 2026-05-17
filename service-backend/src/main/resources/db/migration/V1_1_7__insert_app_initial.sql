INSERT INTO APP_USER (email, name, role, pawd, status, reset_token) VALUES
    ('viewer@email.com', 'Usuario Viewer', 'VIEWER', '$2a$10$KbdhjX3F4l8/ibWNr0TFzus1ROvxUc8iHYsybJIaAEJOIflETefXy', 'ativo', NULL),
    ('approver@email.com', 'Usuario Approver', 'APPROVER', '$2a$10$KbdhjX3F4l8/ibWNr0TFzus1ROvxUc8iHYsybJIaAEJOIflETefXy', 'ativo', NULL),
    ('admin@email.com', 'Usuario Admin', 'ADMIN', '$2a$10$KbdhjX3F4l8/ibWNr0TFzus1ROvxUc8iHYsybJIaAEJOIflETefXy', 'ativo', NULL)
ON CONFLICT (email) DO NOTHING;

INSERT INTO APPLICATION (
    id, name, owner_team, repo_url, created_at, updated_at
) VALUES
    ('00000000-0000-0000-0000-000000000001', 'App 1', 'Team A', 'https://github.com/teamA/app1', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000002', 'App 2', 'Team B', 'https://github.com/teamB/app2', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000003', 'App 3', 'Team C', 'https://github.com/teamC/app3', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000004', 'App 4', 'Team D', 'https://github.com/teamD/app4', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
    ('00000000-0000-0000-0000-000000000005', 'App 5', 'Team E', 'https://github.com/teamE/app5', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO RELEASE (
    id, application_id, version, env, status, evidence_url, version_row, created_at
) VALUES
    ('10000000-0000-0000-0000-000000000001', '00000000-0000-0000-0000-000000000001', '1.0.0', 'production', 'deployed', 'https://evidence.com/release1', 0, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000002', '00000000-0000-0000-0000-000000000001', '1.1.0', 'staging', 'pending', NULL, 0, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000003', '00000000-0000-0000-0000-000000000002', '2.0.0', 'production', 'failed', 'https://evidence.com/release3', 0, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000004', '00000000-0000-0000-0000-000000000003', '3.0.0', 'production', 'deployed', 'https://evidence.com/release4', 0, CURRENT_TIMESTAMP),
    ('10000000-0000-0000-0000-000000000005', '00000000-0000-0000-0000-000000000004', '4.0.0', 'staging', 'pending', NULL, 0, CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO APPROVAL (
    id, release_id, approver_email, outcome, notes, timestamp
) VALUES
    ('20000000-0000-0000-0000-000000000001', '10000000-0000-0000-0000-000000000001', 'approver1@example.com', 'approved', 'Looks good', CURRENT_TIMESTAMP),
    ('20000000-0000-0000-0000-000000000002', '10000000-0000-0000-0000-000000000002', 'approver2@example.com', 'rejected', 'Needs changes', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;

INSERT INTO AUDITLOG (
    id, actor, action, entity, entity_id, payload, timestamp
) VALUES
    ('30000000-0000-0000-0000-000000000001', 'system', 'insert', 'APP_USER', 'viewer@email.com', '{"email": "pendente@email.com"}', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000002', 'system', 'insert', 'APPLICATION', '00000000-0000-0000-0000-000000000001', '{"name": "App 1"}', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000003', 'system', 'insert', 'RELEASE', '10000000-0000-0000-0000-000000000001', '{"version": "1.0.0"}', CURRENT_TIMESTAMP),
    ('30000000-0000-0000-0000-000000000004', 'system', 'insert', 'APPROVAL', '20000000-0000-0000-0000-000000000001', '{"outcome": "approved"}', CURRENT_TIMESTAMP)
ON CONFLICT (id) DO NOTHING;