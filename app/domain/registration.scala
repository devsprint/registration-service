package domain

import java.util.UUID

import repositories.storage.PaginatedResult

import scala.concurrent.Future

object registration {

  sealed trait Gender

  final case object Male extends Gender

  final case object Female extends Gender

  final case class City(name: String) extends AnyVal

  final case class Country(name: String) extends AnyVal

  final case class ZipCode(name: String) extends AnyVal

  final case class Address(streetName: String,
                           number: String,
                           other: String,
                           city: City,
                           zipCode: ZipCode,
                           country: Country)

  final case class PhoneNumber(number: String) extends AnyVal

  final case class Developer(id: Option[UUID],
                             firstName: String,
                             lastName: String,
                             birthYear: Int,
                             gender: Gender,
                             address: Address,
                             phone: PhoneNumber,
                             skills: List[String])

  trait RegistrationService {

    /**
      * Register a new developer
      *
      * @param developer - developer details
      * @return the unique id of the person registered with success, or an error in case that registration failed.
      */
    def create(developer: Developer): Future[UUID]

    /**
      * Retrieve a developer details.
      *
      * @param developerId - unique id of the person
      * @return the person details in case of success or an error
      */
    def read(developerId: UUID): Future[Developer]

    /**
      * Update details of a developer
      * @param developer - the updated developer details
      * @return success or an error
      */
    def update(developer: Developer): Future[UUID]

    /**
      * Remove a registered developer.
      * @param developerId - the unique id of the developer
      * @return success or an error
      */
    def delete(developerId: UUID): Future[Int]

    /**
      * Search for registered developers that match the search text.
      * @param text - the text to search for
      * @return A list of registered persons.
      */
    def search(text: String): Future[List[Developer]]

    /**
      * Retrieve all developers, paginated.
      * @param limit - how many entities to be included in a page
      * @param offset - from where to start.
      * @return a paginated result
      */
    def retrieveAll(limit: Int,
                    offset: Long): Future[PaginatedResult[Developer]]
  }

}
