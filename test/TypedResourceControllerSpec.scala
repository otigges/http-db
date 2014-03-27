import model.{UID, GenericResource}
import org.specs2.mutable.Before
import org.specs2.runner._
import org.junit.runner._

import org.specs2.mutable.Specification
import play.api.test._
import play.api.test.Helpers._
import scala.collection.parallel.mutable
import storage.mock.MockGraphAccess
import testhelper.{WithSingleFakeApp, HttpClient}

@RunWith(classOf[JUnitRunner])
class TypedResourceControllerSpec extends Specification with WithSingleFakeApp with HttpClient {

  sequential

  "TypedResourceController" should {

    "find existing entity" in new freshDB {

      MockGraphAccess.store(new GenericResource(new UID("xyz")))

      val r = get(controllers.routes.TypedResourceController.get("Person", "xyz").url)

      status(r) === OK
    }

    "return Not Found for non existing entity" in new freshDB {

      val r = get(controllers.routes.TypedResourceController.get("Person", "not-there").url)

      status(r) === NOT_FOUND
    }

    "return Not Found for non existing schema" in new freshDB {

      val r = get(controllers.routes.TypedResourceController.get("InvalidSchema", "not-there").url)

      status(r) === NOT_FOUND
    }

    "return Not Found for non existing schema but existing resource" in new freshDB {

      val r = get(controllers.routes.TypedResourceController.get("Person", "xyz").url)

      status(r) === NOT_FOUND
    }

  }

  trait freshDB extends Before {
    def before = MockGraphAccess.clear()
  }

}
