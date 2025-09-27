--liquibase formatted sql

--changeset svtsygankov:2
--comment: переименование таблицы category в categories
ALTER TABLE category RENAME TO categories;

--rollback ALTER TABLE categories RENAME TO category;