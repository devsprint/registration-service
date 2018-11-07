package controllers

import play.api.libs.json.{JsError, JsPath, Json, JsonValidationError}

import scala.concurrent.Future

/**
  * Handle json parsing related errors in controllers.
  */
trait JsonParsingErrorHandler {

  import play.api.mvc.Results._

  /**
    * Build a response message that includes some usefull information.
    * @param errors - errors
    * @return - 400 status with error details.
    */
  def respondWithErrorMessage(errors: Seq[(JsPath, Seq[JsonValidationError])]) =
    Future.successful(
      BadRequest(
        Json.obj("status" -> "Invalid message.",
                 "message" -> JsError.toJson(errors))))
}
