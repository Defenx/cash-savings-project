--liquibase formatted sql
--changeset andreyterex:3
--comment: создание таблицы category с уникальностью (user_id,name,type)
CREATE TABLE IF NOT EXISTS category
(
    id      uuid PRIMARY KEY NOT NULL,
    user_id uuid             NOT NULL,
    name    text             NOT NULL,
    type    text             NOT NULL,
    CONSTRAINT uq_category_user_name_type UNIQUE (user_id, name, type)
);

CREATE INDEX IF NOT EXISTS idx_category_user_id ON category (user_id);

-- rollback DROP INDEX IF EXISTS idx_category_user_id;
-- rollback DROP TABLE IF EXISTS category;