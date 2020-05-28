
# --- !Ups

CREATE TYPE public_state_status AS ENUM ('Created', 'Running', 'Finished');

CREATE TABLE public_states
(
    id                      UUID                NOT NULL PRIMARY KEY,
    name                    varchar(128)        NOT NULL UNIQUE,
    logo                    UUID,
    status                  public_state_status NOT NULL,
    min_citizen_per_state   int                 NOT NULL,
    max_citizen_per_state   int                 NOT NULL,
    started_at              timestamptz,
    market_url              varchar(512),
    is_processing           boolean             NOT NULL,
    ts                      timestamptz         NOT NULL,
    lm                      timestamptz         NOT NULL,
    v                       int                 NOT NULL
);

# --- !Downs

DROP TABLE public_states CASCADE;
DROP TYPE public_state_status;
