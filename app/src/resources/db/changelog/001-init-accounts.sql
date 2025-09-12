CREATE TABLE accounts (
                          id UUID PRIMARY KEY NOT NULL,
                          title TEXT NOT NULL,
                          currency TEXT NOT NULL,
                          amount NUMERIC(19,2) NOT NULL
);