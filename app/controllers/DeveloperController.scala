package controllers

import java.util.UUID

import domain.registration.Developer
import javax.inject.{Inject, Singleton}
import org.slf4j.LoggerFactory
import play.api.libs.json.{JsResult, Json}
import play.api.mvc.{AbstractController, ControllerComponents}
import services.RegistrationService

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.Try

@Singleton
class DeveloperController @Inject()(cc: ControllerComponents,
                                    registrationService: RegistrationService)
    extends AbstractController(cc)
    with JsonParsingErrorHandler {

  private val logger = LoggerFactory.getLogger(getClass)

  import json.support._

  def addDeveloper = Action.async(parse.json) { implicit request =>
    val payload: JsResult[Developer] = request.body.validate[Developer]
    payload
      .fold(
        errors => respondWithErrorMessage(errors),
        developer =>
          registrationService.create(developer).map { id =>
            Created(Json.toJson(id))
        }
      )
      .recover {
        case err: Exception =>
          logger.error("Failed to add developer.", err)
          BadRequest(Json.toJson("Failed to add developer."))
      }
  }

  def updateDeveloper = Action.async(parse.json) { implicit request =>
    val payload: JsResult[Developer] = request.body.validate[Developer]
    payload
      .fold(
        errors => respondWithErrorMessage(errors),
        developer =>
          registrationService.update(developer).map { id =>
            Ok(Json.toJson(id))
        }
      )
      .recover {
        case err: Exception =>
          logger.error("Failed to update developer.", err)
          BadRequest(Json.toJson("Failed to update developer."))
      }
  }

  def removeDeveloper(id: String) = Action.async { implicit request =>
    val developerId = Try { UUID.fromString(id) }.toEither
    developerId.fold(
      errors => {
        logger.error("Invalid id provided", errors)
        Future.successful(
          NotFound(Json.toJson("The id provided is not valid.")))
      },
      devId =>
        registrationService.delete(devId).map { _ =>
          Ok(Json.toJson("Developer has been  removed"))
      }
    )
  }

  def retrieveDeveloper(id: String) = Action.async { implicit request =>
    val developerId = Try { UUID.fromString(id) }.toEither
    developerId.fold(
      errors => {
        logger.error("Invalid id provided", errors)
        Future.successful(
          NotFound(Json.toJson("The id provided is not valid.")))
      },
      devId =>
        registrationService.read(devId).map { developer =>
          Ok(Json.toJson(developer))
      }
    )
  }

}
