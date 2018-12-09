package services
import java.util.UUID

import com.typesafe.config.Config
import domain.registration.Developer
import javax.inject.{Inject, Singleton}
import repositories.{PostgresSQLRepository, storage}
import domain.validation._

import scala.concurrent.Future

@Singleton
class RegistrationService @Inject()(config: Config)
    extends domain.registration.RegistrationService {

  private val postgresConfig = config.getConfig("postgres")
  private val url = postgresConfig.getString("url")
  private val username = postgresConfig.getString("username")
  private val password = postgresConfig.getString("password")

  private val storageRepository = PostgresSQLRepository(url, username, password)

  /**
    * Register a new developer
    *
    * @param developer - developer details
    * @return the unique id of the person registered with success, or an error in case that registration failed.
    */
  override def create(developer: Developer): Future[UUID] = {
    validateDeveloper(developer).toEither match {
      case Right(value) =>
        storageRepository.create(value)
      case Left(err) =>
        throw new Exception(err.toString)
    }
  }

  /**
    * Retrieve a developer details.
    *
    * @param developerId - unique id of the person
    * @return the person details in case of success or an error
    */
  override def read(developerId: UUID): Future[Developer] = {
    storageRepository.read(developerId)
  }

  /**
    * Update details of a developer
    *
    * @param developer - the updated developer details
    * @return success or an error
    */
  override def update(developer: Developer): Future[UUID] = {
    validateDeveloper(developer).toEither match {
      case Right(value) =>
        storageRepository.update(value)
      case Left(err) =>
        throw new Exception(err.toString)
    }
  }

  /**
    * Remove a registered developer.
    *
    * @param developerId - the unique id of the developer
    * @return success or an error
    */
  override def delete(developerId: UUID): Future[Int] = {
    storageRepository.delete(developerId)
  }

  /**
    * Search for registered developers that match the search text.
    *
    * @param text - the text to search for
    * @return A list of registered persons.
    */
  override def search(text: String): Future[List[Developer]] =
    Future.successful(List.empty)

  /**
    * Retrieve all developers, paginated.
    *
    * @param limit  - how many entities to be included in a page
    * @param offset - from where to start.
    * @return a paginated result
    */
  override def retrieveAll(
      limit: Int,
      offset: Long): Future[storage.PaginatedResult[Developer]] =
    storageRepository.findAll(limit, offset)
}
