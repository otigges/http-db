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

}
