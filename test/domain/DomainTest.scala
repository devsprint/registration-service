package domain

import domain.registration.PhoneNumber
import org.scalacheck.{Arbitrary, Gen}
import org.scalatest.{FlatSpec, Matchers}
import org.scalatest.prop.PropertyChecks

class DomainTest extends FlatSpec with Matchers with PropertyChecks {

  "Well known phone number" should "be valid" in {
    val APPLE_SUPPORT_IPHONE = PhoneNumber("+18006947466")
    validation.validatePhone(APPLE_SUPPORT_IPHONE) shouldBe Some(
      APPLE_SUPPORT_IPHONE)
  }

  "Romanian phone numbers using international format" should "generate valid Phone instances" in {
    forAll { phoneNumber: PhoneNumber =>
      validation.validatePhone(phoneNumber) shouldBe Some(phoneNumber)
    }

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
