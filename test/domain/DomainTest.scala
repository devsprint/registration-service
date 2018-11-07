package domain

import cats.data.Chain
import cats.data.Validated.{Invalid, Valid}
import domain.registration._
import domain.validation.NonEmptyStringSmallerThan256Chars
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.PropertyChecks

class DomainTest extends FlatSpec with Matchers with PropertyChecks {

  "Well known phone number" should "be valid" in {
    val APPLE_SUPPORT_IPHONE = PhoneNumber("+18006947466")
    validation.validatePhone(APPLE_SUPPORT_IPHONE) shouldBe Valid(
      APPLE_SUPPORT_IPHONE)
  }

  "Romanian phone numbers using international format" should "generate valid Phone instances" in {
    forAll { phoneNumber: PhoneNumber =>
      validation.validatePhone(phoneNumber) shouldBe Valid(phoneNumber)
    }

  }

  "Empty first name" should "not be validated" in {
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
    validation.validateDeveloper(probe) shouldBe Invalid(
      Chain(NonEmptyStringSmallerThan256Chars("firstName")))
  }

  implicit val arbPhoneNumber: Arbitrary[PhoneNumber] = Arbitrary(
    genPhoneNumber)

  def genPhoneNumber: Gen[PhoneNumber] =
    for {
      countryCode <- Gen.oneOf(List(40))
      areaCode <- Gen.oneOf(List(745, 744, 728))
      lineNumber <- Gen.choose(100000, 999999)
    } yield PhoneNumber(s"+$countryCode$areaCode$lineNumber")

}
