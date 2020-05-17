package controllers

import commons.AppError

object AppErrors {

  val DatabaseError: AppError                     = AppError("db:1", "database error")
  def DatabaseError(error: String): AppError      = DatabaseError.error(error)
  def EntityNotFoundError(name: String): AppError = AppError("db:2", s"entity not found: $name")
  val InvalidJsonPayloadError: AppError           = AppError("app:1", "invalid json payload")
  val AlreadyLoggedInError: AppError              = AppError("auth:1", "already logged in")
  val AuthenticationFailed: AppError              = AppError("auth:2", "authentication failed")
  val InvalidActivationCode: AppError             = AppError("auth:3", "invalid activation code")
  val InvalidUserError: AppError                  = AppError("auth:4", "invalid user. logged in user does not match user referenced via email")
  val MailerError: AppError                       = AppError("app:20", "mailer error")

}
