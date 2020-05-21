package controllers

import commons.AppError

object AppErrors {

  val DatabaseError: AppError                     = AppError("db:1", "database error")
  def DatabaseError(error: String): AppError      = DatabaseError.error(error)
  def EntityNotFoundError(name: String): AppError = AppError("db:2", s"entity not found: $name")
  val InvalidJsonPayloadError: AppError           = AppError("app:1", "invalid json payload")
  val UploadImageError: AppError                  = AppError("app:2", "image upload error")
  val UnsupportedContentTypeError: AppError       = AppError("app:3", "unsupported content type error")
  val MaxFileSizeError: AppError                  = AppError("app:4", "max file size exceeded")
  val NoFileSpecifiedError: AppError              = AppError("app:5", "no file specified")
  val TooManyFilesSpecifiedError: AppError        = AppError("app:6", "too many files specified")
  val InvalidAdminSetupToken: AppError            = AppError("admin:1", "invalid admin setup token")
  val AlreadyLoggedInError: AppError              = AppError("auth:1", "already logged in")
  val AuthenticationFailed: AppError              = AppError("auth:2", "authentication failed")
  val InvalidActivationCode: AppError             = AppError("auth:3", "invalid activation code")
  val InvalidUserError: AppError                  = AppError("auth:4", "invalid user. logged in user does not match user referenced via email")
  val MailerError: AppError                       = AppError("app:20", "mailer error")
  val UnknownS3ServiceError: AppError             = AppError("blob:0", "unknown s3 storage error")
  val AccessS3BucketError: AppError               = AppError("blob:1", "error accessing s3 storage")
}
