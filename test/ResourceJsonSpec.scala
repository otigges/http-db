import org.specs2.mutable._
import org.specs2.runner._
import org.specs2.matcher.JsonMatchers
import org.junit.runner._
import play.api.libs.json.{Json, JsValue}

import model.{GenericResource, QualifiedName, UID, URI}
import play.api.libs.json.Json

/**
 * Spec for all JSON binding of resources.
 */
@RunWith(classOf[JUnitRunner])
class ResourceJsonSpec extends Specification with JsonMatchers {

  "Resources" should {

    "be rendered as JSON" in {

      val uid = UID()
      val json = Json.toJson(new GenericResource(uid))

      json.toString() must /("id" -> uid.id())

    }
  }

}
