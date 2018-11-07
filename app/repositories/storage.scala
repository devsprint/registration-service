package repositories

import java.util.UUID

import domain.registration.Developer

import scala.concurrent.Future

object storage {

  trait StorageRepository {

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
      * @param person - the updated developer details
      * @return success or an error
      */
    def update(developer: Developer): Future[UUID]

    /**
      * Remove a registered developer.
      * @param developerId - the unique id of the developer
      * @return success or an error
      */
    def delete(developerId: UUID): Future[Int]
  }

  trait SearchRepository {

    /**
      * Register a new developer
      *
      * @param developer - developer details
      * @return the unique id of the person registered with success, or an error in case that registration failed.
      */
    def create(developer: Developer): Future[UUID]

    /**
      * Update details of a developer
      * @param developer - the updated developer details
      * @return success or an error
      */
    def update(developer: Developer): Future[Int]

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
  }

}
