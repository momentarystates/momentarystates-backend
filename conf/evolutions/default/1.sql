# create initial schema

# --- !Ups

CREATE TABLE users
(
    id              UUID            NOT NULL PRIMARY KEY,
    username        varchar(128)    NOT NULL,
    password_hash   varchar(4000),
    ts              TIMESTAMP       NOT NULL,
    lm              TIMESTAMP       NOT NULL,
    v               int             NOT NULL
);

# --- !Downs

DROP TABLE users CASCADE;
