import model.{UID, GenericResource}
import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import play.api.test._
import play.api.test.Helpers._
import storage.mock.MockGraphAccess
import testhelper.{WithSingleFakeApp, HttpClient}

@RunWith(classOf[JUnitRunner])
class GenericResourceSpec extends Specification with WithSingleFakeApp with HttpClient {

  "GenericResource" should {

    "find existing resources" in {

      MockGraphAccess.store(new GenericResource(new UID("xyz")))

      val r = get(controllers.routes.GenericResourceController.getGeneric("xyz").url)

      status(r) === OK
    }

    "return Not Found for non existing resources" in {

      val r = get(controllers.routes.GenericResourceController.getGeneric("not-there").url)

      status(r) === NOT_FOUND
    }
  }

}
