package controllers

import javax.inject.Inject
import play.api.mvc.{
  AbstractController,
  Action,
  AnyContent,
  ControllerComponents
}

import scala.concurrent.Future

class HealthCheckController @Inject()(components: ControllerComponents)
    extends AbstractController(components) {

  def livenessCheck: Action[AnyContent] = Action.async {

    Future.successful(Ok)

  }

  def readinessCheck: Action[AnyContent] = Action {
    Ok
  }

}
