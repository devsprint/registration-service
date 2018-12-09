package repositories

import repositories.PatchSupport.Update
import slick.dbio.{DBIOAction, Effect, NoStream}
import slick.lifted.Query
import slick.sql.FixedSqlAction
import slick.jdbc.MySQLProfile.api._

import scala.concurrent.ExecutionContext

import scala.language.higherKinds

object SlickExtensions {
  implicit class RichQuery[Record, U, C[_]](
      val underlying: Query[Record, U, C]) {
    def applyUpdate(
        update: Update[Record]): FixedSqlAction[Int, NoStream, Effect.Write] = {
      update.apply(underlying)
    }

    def applyUpdates(updates: List[Update[Record]])(
        implicit ec: ExecutionContext)
      : DBIOAction[Int, NoStream, Effect.Write with Effect.Read] = {
      updates.reduceLeftOption(_ and _) match {
        case Some(composedUpdate) => underlying.applyUpdate(composedUpdate)
        case None                 => underlying.result.map(_ => 0)
      }
    }
  }
}
