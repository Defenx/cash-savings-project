--liquibase formatted sql

--changeset andreyterex:4
--comment: уникальность аккаунта по (user_id, title, currency)
ALTER TABLE accounts
    ADD CONSTRAINT uq_accounts_user_title_currency
        UNIQUE (user_id, title, currency);

--rollback ALTER TABLE accounts DROP CONSTRAINT IF EXISTS uq_accounts_user_title_currency;