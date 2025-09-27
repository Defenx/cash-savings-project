--liquibase formatted sql

--changeset svtsygankov:5
--comment: добавление индекса ix_operations_account для ускорения запросов по account_id
--preconditions onFail: MARK_RAN
--precondition-sql-check expectedResult:0 SELECT COUNT(*) FROM pg_indexes WHERE tablename = 'operations' AND indexname = 'ix_operations_account'

CREATE INDEX ix_operations_account ON operations (account_id);

--rollback DROP INDEX IF EXISTS ix_operations_account;