import model.schema._
import model.UID
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.libs.json.Json
import play.api.test._
import play.api.test.Helpers._
import storage.mock.{MockSchemaStore}
import testhelper.{WithSingleFakeApp, HttpClient}

@RunWith(classOf[JUnitRunner])
class SchemaControllerSpec extends Specification with WithSingleFakeApp with HttpClient {

  val org: ResourceSchema = ResourceSchema(UID("Organization"), List(
    PropertyDecl("name", DataType.Text, Cardinality.exactlyOne),
    PropertyDecl("location", DataType.Text, Cardinality.zeroOrOne)
  ))

  val person: ResourceSchema = ResourceSchema(UID("Person"), List(
    PropertyDecl("name", DataType.Text, Cardinality.exactlyOne),
    PropertyDecl("favoriteSong", DataType.Text, Cardinality.zeroOrOne),
    PropertyDecl("address", DataType.Complex, Cardinality.zeroOrOne, List(new ComplexAttributeConstraint(List(
      PropertyDecl("street", DataType.Text, Cardinality.exactlyOne),
      PropertyDecl("zip", DataType.Number, Cardinality.exactlyOne),
      PropertyDecl("city", DataType.Text, Cardinality.exactlyOne)
    ))))
  ))

  val project: ResourceSchema = ResourceSchema(UID("Project"), List(
    PropertyDecl("name", DataType.Text, Cardinality.exactlyOne)
  ))

  val validJson = """
               |{
               |    "describes": "Project",
               |    "properties": [
               |        {
               |            "name": "name",
               |            "type": "Text",
               |            "cardinality": "0..*"
               |        }
               |    ]
               |}
             """.stripMargin

  sequential


  "SchemaController" should {

    "find existing schema" in new freshDB {

      MockSchemaStore.store(person)

      val r = get(controllers.routes.SchemaController.get("Person").url)

      status(r) === OK
    }

    "return NotFound for non existing schemas" in new freshDB {

      val r = get(controllers.routes.SchemaController.get("DoesNotExist").url)

      status(r) === NOT_FOUND
    }

    "accept valid schemas via PUT" in new freshDB {

      val r = put(controllers.routes.SchemaController.put("Project").url, Json.parse(validJson))

      status(r) === NO_CONTENT
      MockSchemaStore.findSchema("Project") must beSome
    }

    "accept valid schemas via POST" in new freshDB {

      val r = post(controllers.routes.SchemaController.post().url, Json.parse(validJson))

      status(r) === CREATED
      MockSchemaStore.findSchema("Project") must beSome
    }

    "reject existing schemas via POST" in new freshDB {

      MockSchemaStore.store(project)
      val r = post(controllers.routes.SchemaController.post().url, Json.parse(validJson))
      status(r) === CONFLICT
    }

  }

  trait freshDB extends Before {
    def before = MockSchemaStore.clear()
  }

}
