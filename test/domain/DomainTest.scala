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

  "Phone numbers using international format" should "generate valid Phone instances" in {
    forAll { phoneNumber: PhoneNumber =>
      validation.validatePhone(phoneNumber) shouldBe Some(phoneNumber)
    }

  }

  implicit val arbPhoneNumber: Arbitrary[PhoneNumber] = Arbitrary(
    genPhoneNumber)

  def genPhoneNumber: Gen[PhoneNumber] =
    for {
      countryCode <- Gen.choose(0, 99)
      areaCode <- Gen.choose(0, 99)
      lineNumber <- Gen.choose(0, 99999999)
    } yield PhoneNumber(s"+$countryCode$areaCode$lineNumber")

}
