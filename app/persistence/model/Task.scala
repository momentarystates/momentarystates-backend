package persistence.model

import java.time.OffsetDateTime
import java.util.UUID

case class Task(
    id: Option[UUID],
    privateStateId: UUID,
    name: String,
    description: String,
    ts: OffsetDateTime,
    lm: OffsetDateTime,
    v: Int
)
