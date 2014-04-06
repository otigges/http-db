import model.schema._
import model.UID
import org.specs2.mutable.Specification
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import model._

/**
 * Spec for all JSON binding of resources.
 */
@RunWith(classOf[JUnitRunner])
class ValidationSpec extends Specification {

  val personSchema: ResourceSchema = ResourceSchema(UID("Person"), List(
    PropertyDecl("name", DataType.Text, Cardinality.exactlyOne),
    PropertyDecl("favoriteSong", DataType.Text, Cardinality.zeroOrOne),
    PropertyDecl("address", DataType.Complex, Cardinality.zeroOrOne, List(new ComplexAttributeConstraint(List(
      PropertyDecl("street", DataType.Text, Cardinality.zeroOrOne),
      PropertyDecl("zip", DataType.Number, Cardinality.zeroOrOne),
      PropertyDecl("city", DataType.Text, Cardinality.exactlyOne)
    ))))
  ))

  "SchemaValidator" should {

    "accept valid resources" in {

      val res = new GenericResource(UID(), Seq(
        new TextAttribute("name", "Ed"),
        new TextAttribute("favoriteSong", "Nevermind"),
        new ComplexAttribute("address", Seq(
          new TextAttribute("city", "Berlin")
        ))
      ))

      val result: ValidationResult = new SchemaValidator(personSchema).validate(res)
      result.violations must beEmpty
    }

    "detect missing mandatory fields" in {

      val res = new GenericResource(UID(), Seq(
        new TextAttribute("favoriteSong", "Nevermind")
      ))

      val result: ValidationResult = new SchemaValidator(personSchema).validate(res)
      result.violations must have size 1
      result.violations(0).id must_== "mandatory"
    }

    "detect undeclared fields" in {

      val res = new GenericResource(UID(), Seq(
        new TextAttribute("name", "Ed"),
        new TextAttribute("g1", "A"),
        new TextAttribute("g2", "B")
      ))

      val result: ValidationResult = new SchemaValidator(personSchema).validate(res)
      result.violations must have size 2
      result.violations(0).id must_== "undeclared"
    }

    "detect missing mandatory fields in sub schemas" in {

      val res = new GenericResource(UID(), Seq(
        new TextAttribute("name", "Ed"),
        new ComplexAttribute("address", Seq(
          new TextAttribute("street", "B")
        ))
      ))

      val result: ValidationResult = new SchemaValidator(personSchema).validate(res)
      result.violations must have size 1
      result.violations(0).id must_== "mandatory"
    }

    "detect undeclared fields in sub schemas" in {

      val res = new GenericResource(UID(), Seq(
        new TextAttribute("name", "Ed"),
        new ComplexAttribute("address", Seq(
          new TextAttribute("city", "B"),
          new TextAttribute("timezone", "GMT")
        ))
      ))

      val result: ValidationResult = new SchemaValidator(personSchema).validate(res)
      result.violations must have size 1
      result.violations(0).id must_== "undeclared"
    }

  }

}
