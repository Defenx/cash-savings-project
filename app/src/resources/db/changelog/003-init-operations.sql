--liquibase formatted sql

--changeset svtsygankov:1
CREATE TABLE operations (
                            id UUID NOT NULL,
                            account_id UUID NOT NULL,
                            date DATE NOT NULL,
                            created_date TIMESTAMP WITH TIME ZONE NOT NULL,
                            description TEXT,
                            amount NUMERIC(19,2) NOT NULL,
                            currency TEXT NOT NULL,
                            category_id UUID NOT NULL,
                            PRIMARY KEY (id)
);

ALTER TABLE operations
    ADD CONSTRAINT fk_operations_account
        FOREIGN KEY (account_id)
            REFERENCES accounts(id);

ALTER TABLE operations
    ADD CONSTRAINT fk_operations_category
        FOREIGN KEY (category_id)
            REFERENCES category(id);

--rollback DROP TABLE operations;