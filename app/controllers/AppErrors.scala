package controllers

import commons.BaError

object AppErrors {

  val DatabaseError: BaError                = BaError("database error")
  def DatabaseError(error: String): BaError = BaError(error)
  val InvalidJsonPayloadError: BaError      = BaError("invalid json payload")

}
