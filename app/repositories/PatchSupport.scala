package repositories

import slick.dbio.{Effect, NoStream}
import slick.lifted.{FlatShapeLevel, Query, Shape}
import slick.sql.FixedSqlAction

import slick.jdbc.MySQLProfile.api._
import scala.language.higherKinds

object PatchSupport {

  sealed trait Update[Record] { self =>
    type Field
    type Value

    def field: Record => Field
    def newValue: Value
    def shape: Shape[_ <: FlatShapeLevel, Field, Value, Field]

    final def apply[U, C[_]](query: Query[Record, U, C])
      : FixedSqlAction[Int, NoStream, Effect.Write] = {
      query.map(field)(shape).update(newValue)
    }

    final def and(another: Update[Record]): Update[Record] = {
      new Update[Record] {
        type Field = (self.Field, another.Field)
        type Value = (self.Value, another.Value)

        def field: Record => Field =
          record => (self.field(record), another.field(record))

        def newValue: Value = (self.newValue, another.newValue)

        def shape: Shape[_ <: FlatShapeLevel, Field, Value, Field] = {
          Shape.tuple2Shape(self.shape, another.shape)
        }
      }
    }

  }

  object Update {
    def apply[Record, _Field, _Value](
        _field: Record => _Field,
        _newValue: _Value
    )(
        implicit
        _shape: Shape[_ <: FlatShapeLevel, _Field, _Value, _Field]
    ): Update[Record] = {
      new Update[Record] {
        type Field = _Field
        type Value = _Value

        def field: Record => Field = _field
        def newValue: Value = _newValue
        def shape: Shape[_ <: FlatShapeLevel, Field, Value, Field] = _shape
      }
    }
  }

}
