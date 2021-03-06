package controllers

import java.util.UUID

import domain.registration.{Developer, UpdateDeveloper}
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

  def developers(limit: Option[Int], offset: Option[Long]) = Action.async {
    implicit request =>
      val pageSize = limit.getOrElse(10)
      val startingAt = offset.getOrElse(0L)
      registrationService
        .retrieveAll(pageSize, startingAt)
        .map { page =>
          Ok(Json.toJson(page))
        }
        .recover {
          case err: Exception =>
            logger.error("Failed to retrieve paginated list of developers.",
                         err)
            NotFound(Json.toJson("Failed to retrieve developers collection."))
        }

  }

  def patchDeveloper(id: String) = Action.async(parse.json) {
    implicit request =>
      val payload: JsResult[UpdateDeveloper] =
        request.body.validate[UpdateDeveloper]
      val developerId = Try { UUID.fromString(id) }.toEither

      payload
        .fold(
          errors => respondWithErrorMessage(errors),
          developer =>
            if (developerId.isRight) {
              registrationService
                .patch(developerId.toOption.get,
                       developer.phone,
                       developer.address)
                .map { id =>
                  Ok(Json.toJson(id))
                }
            } else {
              logger.error("Failed to extract developer Id..")
              Future.successful(
                NotFound(Json.toJson("Failed to find developer.")))
          }
        )
        .recover {
          case err: Exception =>
            logger.error("Failed to update developer.", err)
            BadRequest(Json.toJson("Failed to update developer."))
        }
  }

}
