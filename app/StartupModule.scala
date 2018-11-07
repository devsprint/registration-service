import com.google.inject.AbstractModule
import com.typesafe.config.Config
import javax.inject.{Inject, Singleton}
import migration.DbSetup
import org.slf4j.LoggerFactory

@Singleton
class StartupModule @Inject()(config: Config)
    extends AbstractModule
    with DbSetup {
  private val logger = LoggerFactory.getLogger(getClass)

  private val postgresConfig = config.getConfig("postgres")
  val connectionString = postgresConfig.getString("url")
  val username = postgresConfig.getString("username")
  val password = postgresConfig.getString("password")

  override def configure(): Unit = {
    dbSetup()
  }
}
