package repositories

import java.util.UUID

import domain.registration._
import repositories.storage.StorageRepository
import slick.jdbc.{JdbcBackend, JdbcProfile, PostgresProfile}
import slick.jdbc.JdbcBackend._

import scala.concurrent.Future
import scala.concurrent.ExecutionContext.Implicits.global

object PostgresSQLRepository {

  def apply(jdbcUrl: String,
            username: String,
            password: String): PostgresSQLRepository =
    new PostgresSQLRepository(jdbcUrl, username, password)

}

/**
  * PostgresSQL storage support for
  */
class PostgresSQLRepository(jdbcUrl: String, username: String, password: String)
    extends StorageRepository
    with Schema
    with Queries {

  override val db = Database.forURL(jdbcUrl,
                                    username,
                                    password,
                                    driver = "org.postgresql.Driver")
  override val jdbcProfile = PostgresProfile

  /**
    * Register a new developer
    *
    * @param person - developer details
    * @return the unique id of the person registered with success, or an error in case that registration failed.
    */
  override def create(developer: Developer): Future[UUID] =
    insert(developer).map {
      case Some(value) => value
      case None =>
        throw new Exception(s"Failed to store the developer entry: $developer")
    }

  /**
    * Retrieve a developer details.
    *
    * @param developerId - unique id of the person
    * @return the person details in case of success or an error
    */
  override def read(developerId: UUID): Future[Developer] =
    retrieve(developerId).map {
      case Some(value) => value
      case None =>
        throw new Exception(
          s"Failed to retrieve the developer with id: ${developerId.toString}")
    }

  /**
    * Update details of a developer
    *
    * @param developer - the updated developer details
    * @return success or an error
    */
  override def update(developer: Developer): Future[UUID] =
    updateDb(developer).map {
      case Some(value) => value
      case None =>
        throw new Exception(s"Failed to update the developer: $developer")
    }

  /**
    * Remove a registered developer.
    *
    * @param developerId - the unique id of the developer
    * @return success or an error
    */
  override def delete(developerId: UUID): Future[Int] = remove(developerId)

}

trait Schema {
  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._

  implicit lazy val cityColumnType =
    MappedColumnType.base[City, String](_.name, City)
  implicit lazy val countryColumnType =
    MappedColumnType.base[Country, String](_.name, Country)
  implicit lazy val zipColumnType =
    MappedColumnType.base[ZipCode, String](_.name, ZipCode)
  implicit lazy val phoneNumberColumnType =
    MappedColumnType.base[PhoneNumber, String](_.number, PhoneNumber)

  implicit lazy val genderColumnType = MappedColumnType.base[Gender, String](
    {
      case Male   => "male"
      case Female => "female"
    }, {
      case "male"   => Male
      case "female" => Female
      case _        => Male
    }
  )

  implicit lazy val skillsColumnType =
    MappedColumnType.base[List[String], String](
      { skills =>
        skills.mkString(",")
      }, { skills =>
        skills.split(",").toList
      }
    )

  class Developers(tag: Tag) extends Table[Developer](tag, "developers") {
    def id = column[Option[UUID]]("id", O.PrimaryKey)

    def first_name = column[String]("first_name")

    def last_name = column[String]("last_name")

    def birth_year = column[Int]("birth_year")

    def gender = column[Gender]("gender")

    def address_street = column[String]("address_street")

    def address_street_number = column[String]("address_street_number")

    def address_other = column[String]("address_other")

    def address_city = column[City]("address_city")

    def address_zip_code = column[ZipCode]("address_zip_code")

    def address_country = column[Country]("address_country")

    def phone_number = column[PhoneNumber]("phone_number")

    def skills = column[List[String]]("skills")

    private type AddressTupleType =
      (String, String, String, City, ZipCode, Country)
    private type DeveloperTupleType = (Option[UUID],
                                       String,
                                       String,
                                       Int,
                                       Gender,
                                       AddressTupleType,
                                       PhoneNumber,
                                       List[String])

    private val developerShapedValue = (id,
                                        first_name,
                                        last_name,
                                        birth_year,
                                        gender,
                                        (address_street,
                                         address_street_number,
                                         address_other,
                                         address_city,
                                         address_zip_code,
                                         address_country),
                                        phone_number,
                                        skills).shaped

    private val toModel: DeveloperTupleType => Developer = { developerTuple =>
      Developer(
        developerTuple._1,
        developerTuple._2,
        developerTuple._3,
        developerTuple._4,
        developerTuple._5,
        Address.tupled.apply(developerTuple._6),
        developerTuple._7,
        developerTuple._8
      )

    }

    private val toTuple: Developer => Option[DeveloperTupleType] = { dev =>
      Some {
        (
          dev.id,
          dev.firstName,
          dev.lastName,
          dev.birthYear,
          dev.gender,
          Address.unapply(dev.address).get,
          dev.phone,
          dev.skills
        )
      }

    }

    override def * =
      developerShapedValue <> (toModel, toTuple)
  }

  lazy val developers = TableQuery[Developers]

}

trait Queries extends Schema {
  val db: JdbcBackend#DatabaseDef
  val jdbcProfile: JdbcProfile

  import jdbcProfile.api._

  def insert(developer: Developer): Future[Option[UUID]] = {
    val entity = developer.copy(id = Some(UUID.randomUUID()))
    val insertQuery = (developers returning developers.map(_.id)) += entity
    db.run(insertQuery)
  }

  def retrieve(id: UUID): Future[Option[Developer]] = {
    val query = developers.filter(_.id === id).take(1).result
    db.run(query).map(_.headOption)
  }

  def remove(id: UUID): Future[Int] = {
    val query = developers.filter(_.id === id).delete
    db.run(query)
  }

  def updateDb(developer: Developer): Future[Option[UUID]] = {
    val deleteQuery = developers.filter(_.id === developer.id.get).delete
    val insertQuery = (developers returning developers.map(_.id)) += developer
    val transaction = (for {
      _ <- deleteQuery
      _ <- insertQuery
    } yield ()).transactionally
    db.run(transaction).map(_ => developer.id)
  }

}
