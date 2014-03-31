import model.schema.{Cardinality, DataType, PropertyDecl, ResourceSchema}
import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.JsonMatchers
import org.junit.runner._
import play.api.libs.json.{JsObject, Json, JsValue}

import model._

/**
 * Spec for all JSON binding of resources.
 */
@RunWith(classOf[JUnitRunner])
class ResourceJsonSpec extends Specification with JsonMatchers {

  "Resources" should {

    "be rendered as JSON" in {

      val uid = UID()
      val json = Resource.toJson("", new GenericResource(uid, Seq(
        new TextAttribute("name", "n1"),
        new TextAttribute("name2", "n2"),
        new NumberAttribute("age", 42),
        new NumberAttribute("size", 3.21441241241112232232123),
        new BooleanAttribute("y", true),
        new BooleanAttribute("n", false)
      ))).toString()


      json must /("id" -> uid.id())
      json must /("name" -> "n1")
      json must /("name2" -> "n2")
      json must /("age" -> 42)
      json must /("size" -> 3.21441241241112232232123)
      json must /("y" -> true)
      json must /("n" -> false)

    }

    "be parsed from JSON" in {

      val json: JsValue = Json.parse("""{
                                       |  "id" : "abc",
                                       |  "name" : "Joe",
                                       |  "age" : 42,
                                       |  "alive" : true
                                       |}
                                     """.stripMargin)
      
      val resource = GenericResource.fromJson("def", json.as[JsObject])

      resource.qn().id() mustEqual  "def"
      resource.attributes() must have size 3
      resource.attribute("name").value must_== "Joe"
      resource.attribute("age").value must_== 42
      resource.attribute("alive").value must_== true
      resource.attribute("id") must beNull
    }
  }

  "TypedResources" should {

    val personSchema: ResourceSchema = ResourceSchema(UID("Person"), List(
      PropertyDecl("name", DataType.Text, Cardinality.exactlyOne),
      PropertyDecl("favoriteSong", DataType.Text, Cardinality.zeroOrOne)
    ))

    "have schema's attributes order in JSON" in {

      val uid = UID()
      val json = Resource.toJson("", new TypedResource(new GenericResource(uid, Seq(
        new TextAttribute("name", "n1"),
        new TextAttribute("notdefined", "n2")
      )), personSchema)).toString()

      json must /("id" -> uid.id())
      json must /("name" -> "n1")
      json must /("favoriteSong" -> null)
      json must not /("notdefined" -> "n2")

    }
  }

}
