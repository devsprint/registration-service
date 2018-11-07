package domain

import java.time.LocalDate

import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber.PhoneNumber
import domain.registration.{Address, Developer, Phone}

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

  private def validateNonEmptyStringUpTo256Chars(text: String): Option[String] =
    if (text.isEmpty || text.length > 256) None else Some(text)

  private def validateStreetName(streetName: String): Option[String] =
    validateNonEmptyStringUpTo256Chars(streetName)
  private def validateNumberField(number: String): Option[String] =
    if (number.isEmpty || number.length > 10) None else Some(number)
  private def validateOther(other: String): Option[String] =
    if (other.length > 256) None else Some(other)

  private def validateAddress(address: Address): Option[Address] =
    for {
      _ <- validateStreetName(address.streetName)
      _ <- validateNumberField(address.number)
      _ <- validateOther(address.other)
    } yield address

  private def validatePhone(phone: Phone): Option[Phone] = {
    val service = PhoneNumberUtil.getInstance()
    val phoneNumber = service.parseAndKeepRawInput(phone.number, "RO")
    if (service.isValidNumber(phoneNumber)) Some(phone) else None
  }

  private def validateFirstName(firstName: String): Option[String] =
    validateNonEmptyStringUpTo256Chars(
      firstName
    )

  private def validateLastName(lastName: String): Option[String] =
    validateNonEmptyStringUpTo256Chars(lastName)

  private def validateBirthYear(birthYear: Int): Option[Int] =
    if (birthYear >= 1970 && LocalDate.now().minusYears(18).getYear > birthYear)
      Some(birthYear)
    else None

  private def validateSkills(skills: List[String]): Option[List[String]] =
    if (skills.distinct.length > 3) Some(skills) else None

  def validateDeveloper(developer: Developer): Option[Developer] =
    for {
      - <- validateFirstName(developer.firstName)
      _ <- validateLastName(developer.lastName)
      _ <- validateBirthYear(developer.birthYear)
      _ <- validateAddress(developer.address)
      _ <- validatePhone(developer.phone)
      _ <- validateSkills(developer.skills)
    } yield developer

}
