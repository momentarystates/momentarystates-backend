package controllers

import commons.BaError

object AppErrors {

  val DatabaseError: BaError                     = BaError("database error")
  def DatabaseError(error: String): BaError      = BaError(error)
  val InvalidJsonPayloadError: BaError           = BaError("invalid json payload")
  def EntityNotFoundError(name: String): BaError = BaError(s"entity not found: $name")
  val AlreadyLoggedInError: BaError              = BaError("already logged in")
  val AuthenticationFailed: BaError              = BaError("authentication failed")

}
