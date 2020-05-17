package controllers

import commons.BaError

object AppErrors {

  val DatabaseError: BaError                     = BaError("db:1", "database error")
  def DatabaseError(error: String): BaError      = DatabaseError.error(error)
  def EntityNotFoundError(name: String): BaError = BaError("db:2", s"entity not found: $name")

  val InvalidJsonPayloadError: BaError           = BaError("app:1", "invalid json payload")

  val AlreadyLoggedInError: BaError              = BaError("auth:1", "already logged in")
  val AuthenticationFailed: BaError              = BaError("auth:2", "authentication failed")

  val MailerError: BaError                       = BaError("app:20", "mailer error")

}
