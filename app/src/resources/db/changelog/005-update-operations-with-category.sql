--liquibase formatted sql

--changeset svtsygankov:2
--comment: переименование таблицы category в categories
ALTER TABLE category RENAME TO categories;

--rollback ALTER TABLE categories RENAME TO category;

--changeset svtsygankov:3
--comment: добавление колонки category_id в таблицу operations

--ALTER TABLE operations
--    ADD COLUMN category_id UUID NOT NULL;

--rollback ALTER TABLE operations DROP COLUMN IF EXISTS category_id;

--changeset svtsygankov:4
--comment: добавление внешнего ключа fk_operations_category в таблицу operations

ALTER TABLE operations
    ADD CONSTRAINT fk_operations_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT;

CREATE INDEX ix_operations_category ON operations (category_id);

--rollback DROP INDEX IF EXISTS ix_operations_category;
--rollback ALTER TABLE operations DROP CONSTRAINT IF EXISTS fk_operations_category;

--changeset svtsygankov:5
--comment: добавление индекса ix_operations_account для ускорения запросов по account_id

CREATE INDEX ix_operations_account ON operations (account_id);

--rollback DROP INDEX IF EXISTS ix_operations_account;