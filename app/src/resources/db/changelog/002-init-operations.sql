CREATE TABLE operations (
                            id UUID NOT NULL,
                            account_id UUID NOT NULL,
                            date DATE NOT NULL,
                            created_date TIMESTAMP WITHOUT TIME ZONE NOT NULL,
                            description TEXT,
                            amount NUMERIC(19,2) NOT NULL,
                            currency TEXT NOT NULL,
                            PRIMARY KEY (id)
);

ALTER TABLE operations
    ADD CONSTRAINT fk_operations_account
        FOREIGN KEY (account_id)
            REFERENCES accounts(id);