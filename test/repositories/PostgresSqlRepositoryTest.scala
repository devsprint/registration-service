package repositories

import java.sql.DriverManager
import java.util.UUID

import com.dimafeng.testcontainers.{ForAllTestContainer, PostgreSQLContainer}
import domain.registration._
import migration.DbSetup
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.time.{Millis, Seconds, Span}
import org.scalatest.{BeforeAndAfter, FlatSpec, Matchers}
import org.slf4j.LoggerFactory

import scala.collection.immutable
import scala.util.Success
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class PostgresSqlRepositoryTest
    extends FlatSpec
    with Matchers
    with ForAllTestContainer
    with DbSetup
    with ScalaFutures
    with BeforeAndAfter {

  implicit val defaultPatience =
    PatienceConfig(timeout = Span(2, Seconds), interval = Span(5, Millis))

  private val logger = LoggerFactory.getLogger(getClass)

  override val container: PostgreSQLContainer = PostgreSQLContainer()

  private val probe = Developer(
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

  after {
    val repository = PostgresSQLRepository(container.jdbcUrl,
                                           container.username,
                                           container.password)
    repository.removeAll
    ()
  }

  override lazy val username: String = container.username
  override lazy val password: String = container.password
  override lazy val connectionString: String = container.jdbcUrl

  "Storage" should "store valid developer entry" in {
    val repository = PostgresSQLRepository(container.jdbcUrl,
                                           container.username,
                                           container.password)

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

    val resultF = for {
      id <- repository.create(probe)
      developer <- repository.read(id)
    } yield developer

    whenReady(resultF) { result =>
      logger.info("Retrieved developer : {}", result)
      result.copy(id = None) shouldBe probe
    }
  }

  "Storage" should "fail when requested to retrieve a non-existing developer" in {
    val repository = PostgresSQLRepository(container.jdbcUrl,
                                           container.username,
                                           container.password)

    val resultF = repository.read(UUID.randomUUID())

    whenReady(resultF.failed) { e =>
      e shouldBe a[Exception]
    }
  }

  "Storage" should "retrieve all developers in pages" in {
    val repository = PostgresSQLRepository(container.jdbcUrl,
                                           container.username,
                                           container.password)

    val resultF: immutable.Seq[Future[UUID]] =
      (1 to 100).map(_ => repository.create(probe))
    val inserts = Future.sequence(resultF)
    val result = for {
      _ <- inserts
      first <- repository.findAll(10, 0)
    } yield first

    whenReady(result) { page =>
      page.totalCount shouldBe 100
      page.hasNextPage shouldBe true
      page.entities.size shouldBe 10
      page.entities.head.birthYear shouldBe probe.birthYear

    }

  }

}
