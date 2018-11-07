package repositories

import java.sql.DriverManager

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import domain.registration._
import migration.DbSetup
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.{FlatSpec, Matchers}
import org.slf4j.LoggerFactory

import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global

class PostgresSqlRepositoryTest
    extends FlatSpec
    with Matchers
    with ForAllTestContainer
    with DbSetup
    with ScalaFutures {

  private val logger = LoggerFactory.getLogger(getClass)

  override val container: PostgreSQLContainer = PostgreSQLContainer()

  override def afterStart(): Unit = {
    logger.info("after start")
    Class.forName(container.driverClassName)
    val connection =
      DriverManager.getConnection(container.jdbcUrl,
                                  container.username,
                                  container.password)
    logger.debug(s"Connection is available ${connection.getCatalog}")
    dbSetup() match {
      case Success(migrations) =>
        logger.info(s"$migrations has been executed.")
      case scala.util.Failure(exception) =>
        logger.error("Failed to execute migration.", exception)
    }
  }

  override lazy val username: String = container.username
  override lazy val password: String = container.password
  override lazy val connectionString: String = container.jdbcUrl

  "Storage" should "store valid developer entry" in {
    val repository = PostgresSQLRepository(container.jdbcUrl,
                                           container.username,
                                           container.password)
    val probe = Developer(
      None,
      "",
      "Test",
      1971,
      Male,
      Address("Str. Albac",
              "51A",
              "",
              City("Cluj-Napoca"),
              ZipCode("410086"),
              Country("Romania")),
      PhoneNumber("+40745596352"),
      List("java", "scala", "docker", "oop")
    )

    val uuidF = repository.create(probe)

    whenReady(uuidF) { uuid =>
      logger.info("Added developer with UUID: {}", uuid)
      uuid shouldBe uuid
    }

  }

  "Storage" should "retrieve existing developer " in {
    val repository = PostgresSQLRepository(container.jdbcUrl,
                                           container.username,
                                           container.password)
    val probe = Developer(
      None,
      "",
      "Test",
      1971,
      Male,
      Address("Str. Albac",
              "51A",
              "",
              City("Cluj-Napoca"),
              ZipCode("410086"),
              Country("Romania")),
      PhoneNumber("+40745596352"),
      List("java", "scala", "docker", "oop")
    )

    val resultF = for {
      id <- repository.create(probe)
      developer <- repository.retrieve(id)
    } yield developer

    whenReady(resultF) { result =>
      logger.info("Retrieved developer : {}", result)
      result.map(_.copy(id = None)) shouldBe Some(probe)
    }
  }

}
