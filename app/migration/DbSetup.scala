package migration

import org.flywaydb.core.Flyway

import scala.util.Try

trait DbSetup {

  val username: String
  val password: String
  val connectionString: String

  /**
    * Executes db migration
    * @return number of migration in case of success, error otherwise.
    */
  def dbSetup(): Try[Int] = Try {
    val flyWay = new Flyway
    flyWay.setDataSource(connectionString, username, password)
    flyWay.clean()
    flyWay.migrate()
  }

}
