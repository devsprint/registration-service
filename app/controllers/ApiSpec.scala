package controllers

import javax.inject.Inject
import com.iheart.playSwagger.SwaggerSpecGenerator
import play.api.cache.Cached
import play.api.mvc.{AbstractController, ControllerComponents}

import scala.concurrent.{ExecutionContext, Future}

/**
  * Api specification.
  */
class ApiSpec @Inject()(cached: Cached,
                        implicit val ec: ExecutionContext,
                        components: ControllerComponents)
    extends AbstractController(components)
    with JsonParsingErrorHandler {

  implicit val cl = getClass.getClassLoader

  private val domainPackage = Seq("domain.registration")
  private lazy val generator = SwaggerSpecGenerator(true, domainPackage: _*)

  def spec = cached("swagger-cache") {
    Action.async { _ =>
      Future.fromTry(generator.generate()).map(Ok(_))
    }
  }
}
