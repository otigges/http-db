import model.{UID, GenericResource}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import storage.mock.MockGraphAccess
import testhelper.{WithSingleFakeApp, HttpClient}

@RunWith(classOf[JUnitRunner])
class GenericResourceControllerSpec extends Specification with WithSingleFakeApp with HttpClient {

  "GenericResourceController" should {

    "find existing resources" in {

      MockGraphAccess.store(new GenericResource(new UID("xyz")))

      val r = get(controllers.routes.GenericResourceController.getGeneric("xyz").url)

      status(r) === OK
    }

    "return Not Found for non existing resources" in {

      val r = get(controllers.routes.GenericResourceController.getGeneric("not-there").url)

      status(r) === NOT_FOUND
    }

    "add link to existing resources" in {

      val uid = UID("xyz-post-link")

      MockGraphAccess.clear()
      MockGraphAccess.store(new GenericResource())

      val r = post(controllers.routes.GenericResourceController.postLinksGeneric("xyz-post-link").url,
        """[
      {
        "predicate": "a",
        "resource": "c"
      },
      {
        "predicate": "a",
        "resource": "b"
      },
      {
        "predicate": "q",
        "resource": "d"
      }
      ]""")

      status(r) === CREATED
      MockGraphAccess.findResource(uid) must beSome
      // implicitly created resource
      MockGraphAccess.findResource(UID("a")) must beSome
      MockGraphAccess.findResource(UID("z")) must beNone
      MockGraphAccess.findResource(uid).get.associations() must have size 3

    }

    "return Not Found when adding link to non existing resources" in {

      val r = post(controllers.routes.GenericResourceController.postLinksGeneric("not-there").url, "[]")

      status(r) === NOT_FOUND
    }

    "return No Content when adding empty link array to existing resources" in {

      MockGraphAccess.store(new GenericResource(UID("xyz-post-link")))

      val r = post(controllers.routes.GenericResourceController.postLinksGeneric("xyz-post-link").url, "[]")

      status(r) === NO_CONTENT
    }
  }

}
