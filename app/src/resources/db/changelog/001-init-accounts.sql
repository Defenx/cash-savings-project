--liquibase formatted sql

--changeset andreyterex:1
--comment: создание таблицы accounts
CREATE TABLE accounts (
                          id UUID PRIMARY KEY NOT NULL,
                          title TEXT NOT NULL,
                          currency TEXT NOT NULL,
                          amount NUMERIC(19,2) NOT NULL
);
--rollback DROP TABLE accounts