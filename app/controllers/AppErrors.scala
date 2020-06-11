package controllers

import commons.AppError

object AppErrors {

  val DatabaseError: AppError                          = AppError("db:1", "database error")
  def DatabaseError(error: String): AppError           = DatabaseError.error(error)
  def EntityNotFoundError(name: String): AppError      = AppError("db:2", s"entity not found: $name")
  val InvalidJsonPayloadError: AppError                = AppError("app:1", "invalid json payload")
  val UploadImageError: AppError                       = AppError("app:2", "image upload error")
  val UnsupportedContentTypeError: AppError            = AppError("app:3", "unsupported content type error")
  val MaxFileSizeError: AppError                       = AppError("app:4", "max file size exceeded")
  val NoFileSpecifiedError: AppError                   = AppError("app:5", "no file specified")
  val TooManyFilesSpecifiedError: AppError             = AppError("app:6", "too many files specified")
  val NotFoundError: AppError                          = AppError("app:7", "404 not found")
  val InvalidQueryStringError: AppError                = AppError("app:8", "invalid query string")
  val InvalidAdminSetupToken: AppError                 = AppError("admin:1", "invalid admin setup token")
  val AlreadyLoggedInError: AppError                   = AppError("auth:1", "already logged in")
  val AuthenticationFailed: AppError                   = AppError("auth:2", "authentication failed")
  val InvalidActivationCode: AppError                  = AppError("auth:3", "invalid activation code")
  val InvalidUserError: AppError                       = AppError("auth:4", "invalid user. logged in user does not match user referenced via email")
  val MailerError: AppError                            = AppError("app:20", "mailer error")
  val UnknownS3ServiceError: AppError                  = AppError("blob:0", "unknown s3 storage error")
  val AccessS3BucketError: AppError                    = AppError("blob:1", "error accessing s3 storage")
  val InvalidSpeculationTokenError: AppError           = AppError("game:1", "invalid speculation token");
  val InvalidGoddessError: AppError                    = AppError("game:2", "requesting user is not goddess of public state")
  val InvalidPublicStateStatusError: AppError          = AppError("game:3", "the public state is in a wrong status")
  val GoddessCantCreatePrivateStateError: AppError     = AppError("game:4", "a goddess can't create a private state")
  val AlreadyActiveCitizenError: AppError              = AppError("game:5", "the user is already an active citizen in this public state")
  val NotCitizenOfPrivateStateError: AppError          = AppError("game:6", "the user is not citizen of the private state")
  val ForbiddenDueToSocialOrderError: AppError         = AppError("game:7", "this action is not possible due to social order of private state")
  val MaxCitizenPerPrivateStateError: AppError         = AppError("game:8", "maximum number of citizen per private state reached")
  val InvalidJoinPrivateStateTokenError: AppError      = AppError("game:9", "invalid token to join the private state")
  val JoinPrivateStateInviteAlreadyUsedError: AppError = AppError("game:10", "this token has already been used to join a private state")
}
