
# --- !Ups

CREATE TABLE speculations
(
    id                      UUID                NOT NULL PRIMARY KEY,
    email                   varchar(320)        NOT NULL,
    token                   varchar(16)         NOT NULL,
    ts                      timestamptz         NOT NULL,
    lm                      timestamptz         NOT NULL,
    v                       int                 NOT NULL
);

CREATE TYPE public_state_status AS ENUM ('Created', 'Running', 'Finished');

CREATE TABLE public_states
(
    id                      UUID                NOT NULL PRIMARY KEY,
    speculation_id          UUID                NOT NULL REFERENCES "speculations",
    name                    varchar(128)        NOT NULL UNIQUE,
    logo                    UUID,
    goddess                 UUID                NOT NULL REFERENCES "users",
    status                  public_state_status NOT NULL,
    started_at              timestamptz,
    market_url              varchar(512),
    params                  json                NOT NULL,
    is_processing           boolean             NOT NULL,
    ts                      timestamptz         NOT NULL,
    lm                      timestamptz         NOT NULL,
    v                       int                 NOT NULL
);

ALTER TABLE speculations ADD COLUMN public_state_id UUID REFERENCES "public_states";

CREATE TYPE social_order AS ENUM ('SinglePerson', 'SimpleMajority', 'Consensus', 'SinglePersonRotation');

CREATE TABLE private_states
(
    id                      UUID                NOT NULL PRIMARY KEY,
    public_state_id         UUID                NOT NULL REFERENCES "public_states",
    name                    varchar(128)        NOT NULL UNIQUE,
    logo                    UUID,
    social_order            social_order        NOT NULL,
    created_by              UUID                NOT NULL REFERENCES "users",
    ts                      timestamptz         NOT NULL,
    lm                      timestamptz         NOT NULL,
    v                       int                 NOT NULL
);

CREATE TABLE citizens
(
    id                      UUID                NOT NULL PRIMARY KEY,
    user_id                 UUID                NOT NULL REFERENCES "users",
    private_state_id        UUID                NOT NULL REFERENCES "private_states",
    started_at              timestamptz         NOT NULL,
    ended_at                timestamptz,
    ts                      timestamptz         NOT NULL,
    lm                      timestamptz         NOT NULL,
    v                       int                 NOT NULL
);

ALTER TABLE private_states ADD COLUMN master UUID REFERENCES "citizens";

ALTER TABLE private_states ADD COLUMN journalist UUID REFERENCES "citizens";


# --- !Downs

DROP TABLE speculations CASCADE;
DROP TABLE public_states CASCADE;
DROP TYPE public_state_status;
DROP TABLE private_states CASCADE;
DROP TYPE social_order;
DROP TABLE citizens CASCADE;
