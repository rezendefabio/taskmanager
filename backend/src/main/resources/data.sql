-- Usuario Admin
INSERT INTO users (name, email, password, role) VALUES ('Admin', 'admin@elotech.com', '$2a$10$uUOlkw45nDhSPhpgaeRgyOPz.nCjDv14FW4bp6.PtJlLLmEaI7FOC', 'ADMIN');

-- Usuario Member
INSERT INTO users (name, email, password, role) VALUES ('Membro', 'membro@elotech.com', '$2a$10$uUOlkw45nDhSPhpgaeRgyOPz.nCjDv14FW4bp6.PtJlLLmEaI7FOC', 'MEMBER');

-- Projeto
INSERT INTO projects (name, description, owner_id) VALUES ('Projeto Demo', 'Projeto de demonstracao do Task Manager', 1);

-- Membros do projeto
INSERT INTO project_members (project_id, user_id) VALUES (1, 1);
INSERT INTO project_members (project_id, user_id) VALUES (1, 2);

-- Tarefas
INSERT INTO tasks (title, description, status, priority, created_at, updated_at, deadline, assignee_id, project_id) VALUES ('Implementar autenticacao', 'Criar login com JWT', 'DONE', 'HIGH', NOW(), NOW(), '2026-04-20 18:00:00', 1, 1);
INSERT INTO tasks (title, description, status, priority, created_at, updated_at, deadline, assignee_id, project_id) VALUES ('Criar dashboard', 'Dashboard com metricas do projeto', 'IN_PROGRESS', 'MEDIUM', NOW(), NOW(), '2026-04-22 18:00:00', 2, 1);
INSERT INTO tasks (title, description, status, priority, created_at, updated_at, deadline, assignee_id, project_id) VALUES ('Configurar CI/CD', 'Pipeline de deploy automatico', 'TODO', 'LOW', NOW(), NOW(), NULL, NULL, 1);
INSERT INTO tasks (title, description, status, priority, created_at, updated_at, deadline, assignee_id, project_id) VALUES ('Corrigir bug critico', 'Bug no calculo de relatorio', 'TODO', 'CRITICAL', NOW(), NOW(), '2026-04-18 12:00:00', 1, 1);
INSERT INTO tasks (title, description, status, priority, created_at, updated_at, deadline, assignee_id, project_id) VALUES ('Escrever testes unitarios', 'Cobrir regras de negocio com testes', 'IN_PROGRESS', 'HIGH', NOW(), NOW(), '2026-04-19 18:00:00', 1, 1);
