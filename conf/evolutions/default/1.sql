# create initial schema

# --- !Ups

CREATE TYPE user_role AS ENUM ('User', 'Admin');

CREATE TABLE users
(
    id              UUID            NOT NULL PRIMARY KEY,
    username        varchar(128)    NOT NULL UNIQUE,
    password_hash   varchar(64)     NOT NULL,
    password_salt   varchar(16)     NOT NULL,
    role            user_role       NOT NULL,
    ts              timestamptz     NOT NULL,
    lm              timestamptz     NOT NULL,
    v               int             NOT NULL
);

CREATE TABLE auth_tokens
(
    id              UUID            NOT NULL PRIMARY KEY,
    token           varchar(36)     NOT NULL UNIQUE,
    user_id         UUID            NOT NULL,
    valid           boolean         NOT NULL,
    expires         timestamptz,
    remote_address  varchar(46)     NOT NULL,
    ts              timestamptz     NOT NULL,
    lm              timestamptz     NOT NULL,
    v               int             NOT NULL
)

# --- !Downs

DROP TABLE users CASCADE;
DROP TYPE user_role;
DROP TABLE auth_tokens CASCADE;
