package domain

import java.time.LocalDate

import cats.data.ValidatedNec
import cats.implicits._
import com.google.i18n.phonenumbers.PhoneNumberUtil
import domain.registration.{Address, Developer, PhoneNumber}

import scala.util.Try

object validation {

  // Address
  // 1. streetName field should not be empty and hax max 256 characters
  // 2. number field should not be empty and has max 10 characters
  // 3. other field could be empty and may contains up to 256 characters

  // Phone
  // 1. phone number should be valid according

  // Developer
  // 1. firstName, lastName field should not be empty. It should have max 256  characters
  // 2. birth year should be over 1970, and the developer should be over 18 years old.
  // 3. skills should contains at least 3 different skills

  sealed trait DomainValidation {
    def errorMessage: String
  }

  type ValidationResult[A] = ValidatedNec[DomainValidation, A]

  private val MAX_FIELD_LENGTH: Int = 256
  private val MAX_STREET_NUMBER_FIELD_LENGTH: Int = 256
  private val MIN_BIRTH_YEAR: Int = 1970
  private val DEVELOPER_MIN_AGE: Int = 18

  case class NonEmptyStringSmallerThan256Chars(fieldName: String)
      extends DomainValidation {
    override def errorMessage: String =
      s"The value supplied for $fieldName is empty or larger than $MAX_FIELD_LENGTH"
  }

  case class EmptyStringSmallerThan256Chars(fieldName: String)
      extends DomainValidation {
    override def errorMessage: String =
      s"The value supplied for $fieldName is larger than $MAX_FIELD_LENGTH"
  }

  case object InvalidStreetNumberField extends DomainValidation {
    override def errorMessage: String =
      s"Street number field is empty or larger than $MAX_STREET_NUMBER_FIELD_LENGTH"
  }

  case object InvalidSkillSet extends DomainValidation {
    override def errorMessage: String =
      s"There should be at least 3 skills in the set of skills."
  }

  case class InvalidPhoneNumber(message: String) extends DomainValidation {
    override def errorMessage: String = s"Invalid phone number. $message"
  }

  case object InvalidBirthYear extends DomainValidation {
    override def errorMessage: String =
      "The birth year should be after 1970 and the age of the developer should be over 18."
  }

  def validateDeveloper(developer: Developer): ValidationResult[Developer] =
    (validateFirstName(developer.firstName),
     validateLastName(developer.lastName),
     validateBirthYear(developer.birthYear),
     validateAddress(developer.address),
     validatePhone(developer.phone),
     validateSkills(developer.skills))
      .mapN(Developer(developer.id, _, _, _, developer.gender, _, _, _))

  private def validateFirstName(firstName: String): ValidationResult[String] =
    validateNonEmptyStringUpTo256Chars(
      firstName,
      "firstName"
    )

  private def validateLastName(lastName: String): ValidationResult[String] =
    validateNonEmptyStringUpTo256Chars(lastName, "lastName")

  private def validateBirthYear(birthYear: Int): ValidationResult[Int] =
    if (birthYear >= MIN_BIRTH_YEAR && LocalDate
          .now()
          .minusYears(DEVELOPER_MIN_AGE)
          .getYear > birthYear)
      birthYear.validNec
    else InvalidBirthYear.invalidNec

  private def validateAddress(address: Address): ValidationResult[Address] =
    (validateStreetName(address.streetName),
     validateStreetNumberField(address.number),
     validateOther(address.other))
      .mapN(Address(_, _, _, address.city, address.zipCode, address.country))

  def validatePhone(phone: PhoneNumber): ValidationResult[PhoneNumber] = {
    val service = PhoneNumberUtil.getInstance()
    val phoneNumber =
      Try {
        service.parse(phone.number, "")
      }
    phoneNumber
      .map { pn =>
        if (service.isValidNumber(pn)) phone.validNec
        else InvalidPhoneNumber("Validation failed").invalidNec
      } match {
      case scala.util.Success(validService) => validService
      case scala.util.Failure(exception) =>
        InvalidPhoneNumber(exception.getMessage).invalidNec
    }

  }

  private def validateSkills(
      skills: List[String]): ValidationResult[List[String]] =
    if (skills.distinct.length > 3) skills.validNec
    else InvalidSkillSet.invalidNec

  private def validateStreetName(streetName: String): ValidationResult[String] =
    validateNonEmptyStringUpTo256Chars(streetName, "streetName")

  private def validateStreetNumberField(
      number: String): ValidationResult[String] =
    if (number.isEmpty || number.length > MAX_STREET_NUMBER_FIELD_LENGTH)
      InvalidStreetNumberField.invalidNec
    else number.validNec

  private def validateOther(other: String): ValidationResult[String] =
    if (other.length > MAX_FIELD_LENGTH)
      EmptyStringSmallerThan256Chars("other").invalidNec
    else other.validNec

  private def validateNonEmptyStringUpTo256Chars(
      text: String,
      fieldName: String): ValidationResult[String] =
    if (text.isEmpty || text.length > MAX_FIELD_LENGTH)
      NonEmptyStringSmallerThan256Chars(fieldName).invalidNec
    else text.validNec

}
