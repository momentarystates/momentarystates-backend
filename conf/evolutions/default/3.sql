

# --- !Ups

CREATE TABLE create_private_state_invites
(
    id                      UUID                NOT NULL PRIMARY KEY,
    public_state_id         UUID                NOT NULL REFERENCES "public_states",
    email                   varchar(320)        NOT NULL UNIQUE,
    token                   varchar(16)         NOT NULL,
    used_at                 timestamptz,
    used_by                 UUID                REFERENCES "users",
    ts                      timestamptz         NOT NULL,
    lm                      timestamptz         NOT NULL,
    v                       int                 NOT NULL
);


CREATE TABLE join_private_state_invites
(
    id                      UUID                NOT NULL PRIMARY KEY,
    private_state_id        UUID                NOT NULL REFERENCES "private_states",
    email                   varchar(320)        NOT NULL UNIQUE,
    token                   varchar(16)         NOT NULL,
    used_at                 timestamptz,
    used_by                 UUID                REFERENCES "users",
    ts                      timestamptz         NOT NULL,
    lm                      timestamptz         NOT NULL,
    v                       int                 NOT NULL
);


# --- !Downs

DROP TABLE create_private_state_invites CASCADE;
DROP TABLE join_private_state_invites CASCADE;
