package json

import domain.registration._
import play.api.libs.json._
import repositories.storage.PaginatedResult

object support {

  implicit val cityFormat = Json.format[City]
  implicit val zipCodeFormat = Json.format[ZipCode]
  implicit val countryFormat = Json.format[Country]
  implicit val addressFormat = Json.format[Address]
  implicit val phoneNumberFormat = Json.format[PhoneNumber]

  implicit object GenderReads extends Reads[Gender] {
    override def reads(gender: JsValue): JsResult[Gender] =
      gender.as[String] match {
        case "Male"   => JsSuccess(Male)
        case "Female" => JsSuccess(Female)
        case _        => JsError("Only Male/Female are acceptable values for Gender.")
      }
  }

  implicit object GenderWrites extends Writes[Gender] {
    override def writes(gender: Gender): JsValue = gender match {
      case Male   => JsString("Male")
      case Female => JsString("Female")
    }
  }

  implicit val developerFormat = Json.format[Developer]

  implicit val pageResultFormat = Json.format[PaginatedResult[Developer]]

  implicit val updateDeveloperFormat = Json.format[UpdateDeveloper]

}
