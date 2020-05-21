# create initial schema

# --- !Ups

CREATE TYPE user_role AS ENUM ('User', 'Admin');

CREATE TABLE users
(
    id                  UUID            NOT NULL PRIMARY KEY,
    username            varchar(128)    NOT NULL UNIQUE,
    password_hash       varchar(64)     NOT NULL,
    password_salt       varchar(16)     NOT NULL,
    role                user_role       NOT NULL,
    email               varchar(320)    NOT NULL UNIQUE,
    email_confirmed_at  timestamptz,
    confirmation_code   varchar(6)      NOT NULL,
    avatar              UUID,
    ts                  timestamptz     NOT NULL,
    lm                  timestamptz     NOT NULL,
    v                   int             NOT NULL
);

CREATE TABLE auth_tokens
(
    id                  UUID            NOT NULL PRIMARY KEY,
    token               varchar(36)     NOT NULL UNIQUE,
    user_id             UUID            NOT NULL,
    valid               boolean         NOT NULL,
    expires             timestamptz,
    remote_address      varchar(46)     NOT NULL,
    ts                  timestamptz     NOT NULL,
    lm                  timestamptz     NOT NULL,
    v                   int             NOT NULL
);

CREATE TYPE email_status AS ENUM ('Sent', 'Unsent');

CREATE TABLE emails
(
    id                  UUID            NOT NULL PRIMARY KEY,
    subject             varchar(256)    NOT NULL,
    recipients          varchar ARRAY   NOT NULL,
    body                varchar         NOT NULL,
    status              email_status    NOT NULL,
    message_id          varchar(64),
    retries             int             NOT NULL,
    ts                  timestamptz     NOT NULL,
    lm                  timestamptz     NOT NULL,
    v                   int             NOT NULL
);

CREATE TABLE app_params
(
    id                  UUID            NOT NULL PRIMARY KEY,
    key                 varchar(256)    NOT NULL,
    value               varchar         NOT NULL,
    ts                  timestamptz     NOT NULL,
    lm                  timestamptz     NOT NULL,
    v                   int             NOT NULL
);

DO $$
DECLARE fake_id UUID;;
DECLARE ts TIMESTAMP;;
BEGIN
    SELECT md5(random()::text || clock_timestamp()::text)::uuid INTO fake_id;;
	SELECT CURRENT_TIMESTAMP INTO ts;;
	EXECUTE format('INSERT INTO app_params (id, key, value, ts, lm, v) VALUES (%L, %L, %L, %L, %L, %L);;', (fake_id), ('emailWorkerBusy'), ('0'), (ts), (ts), (0));;
END;;
$$ LANGUAGE plpgsql;

CREATE TABLE binaries
(
    id                  UUID            NOT NULL PRIMARY KEY,
    file_name           varchar(256)    NOT NULL,
    path                varchar(256)    NOT NULL,
    content_type        varchar(64)     NOT NULL,
    length              bigint          NOT NULL,
    md5                 varchar(32)     NOT NULL,
    ts                  timestamptz     NOT NULL,
    lm                  timestamptz     NOT NULL,
    v                   int             NOT NULL
)


# --- !Downs

DROP TABLE users CASCADE;
DROP TYPE user_role;
DROP TABLE auth_tokens CASCADE;
DROP TABLE emails CASCADE;
DROP TYPE email_status;
DROP TABLE app_params CASCADE;
DROP TABLE binaries CASCADE;
