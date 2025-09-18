--liquibase formatted sql

--changeset andreyterex:2
--comment: создание таблиц users, roles, user_roles, добавление user_id в accounts, индексы и сиды
--USERS
CREATE TABLE users
(
    id           UUID PRIMARY KEY NOT NULL,
    email        TEXT             NOT NULL,
    password     TEXT             NOT NULL,
    created_date timestamptz      NOT NULL,
    updated_date timestamptz      NOT NULL
);
ALTER TABLE users
    ADD CONSTRAINT uq_users_email UNIQUE (email);

--ROLES
CREATE TABLE roles
(
    id   UUID PRIMARY KEY NOT NULL,
    role_name TEXT NOT NULL
);
ALTER TABLE roles
    ADD CONSTRAINT uq_roles_name UNIQUE (role_name);

CREATE TABLE user_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id),
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id)
);

-- SEEDS FOR ROLES
INSERT INTO roles (id, role_name)
VALUES (gen_random_uuid(), 'USER')
ON CONFLICT ON CONSTRAINT uq_roles_name DO NOTHING;
INSERT INTO roles (id, role_name)
VALUES (gen_random_uuid(), 'SUPPORT')
ON CONFLICT ON CONSTRAINT uq_roles_name DO NOTHING;

ALTER TABLE accounts
    ADD COLUMN user_id UUID;

ALTER TABLE accounts
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE accounts
    ADD CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;

CREATE INDEX ix_accounts_user ON accounts (user_id);
-- rollback DROP INDEX IF EXISTS ix_accounts_user;
-- rollback ALTER TABLE accounts DROP CONSTRAINT IF EXISTS fk_accounts_user;
-- rollback ALTER TABLE accounts ALTER COLUMN user_id DROP NOT NULL;
-- rollback ALTER TABLE accounts DROP COLUMN IF EXISTS user_id;
-- rollback DROP TABLE IF EXISTS user_roles;
-- rollback DROP TABLE IF EXISTS roles;
-- rollback DROP TABLE IF EXISTS users;