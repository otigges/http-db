import model.schema._
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.JsonMatchers
import org.junit.runner._
import play.api.libs.json.{JsObject, Json, JsValue}

import model._

/**
 * Spec for all JSON binding of schemas.
 */
@RunWith(classOf[JUnitRunner])
class SchemaJsonSpec extends Specification with JsonMatchers {

  private val simpleSchema: ResourceSchema = ResourceSchema(UID("Organization"), List(
    PropertyDecl("name", DataType.Text, Cardinality.exactlyOne),
    PropertyDecl("location", DataType.Text, Cardinality.zeroOrOne)
  ))

  private val complex: ResourceSchema = ResourceSchema(UID("Person"), List(
    PropertyDecl("name", DataType.Text, Cardinality.exactlyOne),
    PropertyDecl("favoriteSong", DataType.Text, Cardinality.zeroOrOne),
    PropertyDecl("address", DataType.Complex, Cardinality.zeroOrOne, List(new ComplexAttributeConstraint(List(
      PropertyDecl("street", DataType.Text, Cardinality.exactlyOne),
      PropertyDecl("zip", DataType.Number, Cardinality.exactlyOne),
      PropertyDecl("city", DataType.Text, Cardinality.exactlyOne)
    ))))
  ))


  "Simple schema" should {

    "be rendered as JSON" in {

      val json: String = ResourceSchema.toJson(simpleSchema).toString()

      json must /("describes" -> "Organization")
      json must /("properties") /# 0 /("name" -> "name")
      json must /("properties") /# 0 /("type" -> "Text")
      json must /("properties") /# 0 /("cardinality" -> "1..1")
      json must /("properties") /# 1 /("name" -> "location")
      json must /("properties") /# 1 /("type" -> "Text")
      json must /("properties") /# 1 /("cardinality" -> "0..1")

    }
  }

  "Complex schema" should {

    "be rendered as JSON" in {

      val json: String = ResourceSchema.toJson(complex).toString()

      println(json)

      json must /("describes" -> "Person")
      json must /("properties") /# 0 /("name" -> "name")
      json must /("properties") /# 1 /("name" -> "favoriteSong")
      json must /("properties") /# 2 /("name" -> "address")
      json must /("properties") /# 2 /("type" -> "Complex")

    }
  }

  "Cardinality ranges" should {

    "be parsed correctly" in {
      val cardinality1 = Cardinality.parse("2..5").right.get
      val cardinality2 = Cardinality.parse("0..112412424").right.get
      val cardinality3 = Cardinality.parse("*").right.get
      val cardinality4 = Cardinality.parse("1..*").right.get
      val cardinality5 = Cardinality.parse("3..n").right.get

      cardinality1.min mustEqual 2
      cardinality1.max mustEqual 5
      cardinality2.min mustEqual 0
      cardinality2.max mustEqual 112412424
      cardinality3.min mustEqual 0
      cardinality3.max mustEqual Int.MaxValue
      cardinality3.unbound must_== true
      cardinality4.min mustEqual 1
      cardinality4.max mustEqual Int.MaxValue
      cardinality4.unbound must_== true
      cardinality5.min mustEqual 3
      cardinality5.max mustEqual Int.MaxValue
      cardinality5.unbound must_== true
    }
  }

}
