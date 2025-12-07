--liquibase formatted sql

--changeset andreyterex:1
--comment: создание таблиц users, roles, user_roles и сиды

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
    id        UUID PRIMARY KEY NOT NULL,
    role_name TEXT             NOT NULL
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

--changeset allavrublevskaja:2
--comment: создание таблиц accounts, entries, roles, transactions, users, user_roles

create table accounts
(
    id           uuid primary key not null,
    title        text             not null,
    currency     text             not null,
    account_type text             not null,
    user_id      uuid             not null,
    constraint uk_accounts_user_name_type_currency
        unique (user_id, title, account_type, currency)
);

alter table if exists accounts
    add constraint FKnjuop33mo69pd79ctplkck40n
        foreign key (user_id)
            references users;

CREATE INDEX ix_accounts_user ON accounts (user_id);

create table transactions
(
    id           uuid primary key not null,
    date         date             not null,
    created_date timestamptz      not null,
    description  text             not null,
    user_id      uuid             not null
);

alter table if exists transactions
    add constraint FKqwv7rmvc8va8rep7piikrojds
        foreign key (user_id)
            references users;

create table entries
(
    id             uuid primary key not null,
    entry_type     text             not null,
    amount         numeric(19, 2)   not null,
    transaction_id uuid             not null,
    account_id     uuid             not null
);

alter table if exists entries
    add constraint fk_entries_transaction
        foreign key (transaction_id)
            references transactions (id) ON DELETE CASCADE;

alter table if exists entries
    add constraint fk_entries_account
        foreign key (account_id)
            references accounts (id) ON DELETE RESTRICT;
