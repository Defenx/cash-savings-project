--liquibase formatted sql

--changeset svtsygankov:3
--comment: добавление колонки category_id в таблицу operations
--preconditions onFail: MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.columns WHERE table_name = 'operations' AND column_name = 'category_id'

ALTER TABLE operations
    ADD COLUMN category_id UUID NOT NULL;

--rollback ALTER TABLE operations DROP COLUMN IF EXISTS category_id;

--changeset svtsygankov:4
--comment: добавление внешнего ключа fk_operations_category в таблицу operations
--preconditions onFail: MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM information_schema.table_constraints WHERE table_name = 'operations' AND constraint_name = 'fk_operations_category'

ALTER TABLE operations
    ADD CONSTRAINT fk_operations_category
        FOREIGN KEY (category_id) REFERENCES categories(id) ON DELETE RESTRICT;

CREATE INDEX ix_operations_category ON operations (category_id);

--rollback DROP INDEX IF EXISTS ix_operations_category;
--rollback ALTER TABLE operations DROP CONSTRAINT IF EXISTS fk_operations_category;