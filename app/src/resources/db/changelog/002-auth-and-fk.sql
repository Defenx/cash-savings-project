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
    name TEXT             NOT NULL
);
ALTER TABLE roles
    ADD CONSTRAINT uq_roles_name UNIQUE (name);

CREATE TABLE user_roles
(
    user_id UUID NOT NULL,
    role_id UUID NOT NULL,
    PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_roles_user FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_roles_role FOREIGN KEY (role_id) REFERENCES roles (id) ON DELETE CASCADE
);

-- SEEDS FOR ROLES
INSERT INTO roles (id, name)
VALUES ('11111111-1111-1111-1111-111111111111', 'USER')
ON CONFLICT ON CONSTRAINT uq_roles_name DO NOTHING;
INSERT INTO roles (id, name)
VALUES ('22222222-2222-2222-2222-222222222222', 'SUPPORT')
ON CONFLICT ON CONSTRAINT uq_roles_name DO NOTHING;

ALTER TABLE accounts
    ADD COLUMN user_id UUID;

ALTER TABLE accounts
    ALTER COLUMN user_id SET NOT NULL;

ALTER TABLE accounts
    ADD CONSTRAINT fk_accounts_user
        FOREIGN KEY (user_id) REFERENCES users (id) ON DELETE RESTRICT;

CREATE INDEX ix_accounts_user ON accounts (user_id);