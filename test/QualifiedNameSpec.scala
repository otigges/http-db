import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._

import model.{QualifiedName, UID, URI}

/**
 * Spec for all kinds of qualified names.
 */
@RunWith(classOf[JUnitRunner])
class QualifiedNameSpec extends Specification {

  val uri1 = "http://example.org/sample/thing"
  val uri2 = "http://example.org/sample#thing"
  val uri3 = "http://example.org/sample/"

  val localUidNamespace = "local:uid:"

  "URIs" should {

    "provide local names" in {

      new URI(uri1).getLocalName() mustEqual "thing"
      new URI(uri2).getLocalName() mustEqual "thing"
      new URI(uri3).getLocalName() mustEqual ""

    }

    "provide namespaces" in {

      new URI(uri1).getNamespace() mustEqual "http://example.org/sample/"
      new URI(uri2).getNamespace() mustEqual "http://example.org/sample#"
      new URI(uri3).getNamespace() mustEqual "http://example.org/sample/"

    }

  }

  "UIDs" should {

    "provide local name" in {

      new UID("xyz").getLocalName() mustEqual "xyz"
      new UID().getLocalName() must not beEmpty

    }

    "provide namespaces" in {

      new UID("xyz").getNamespace() mustEqual localUidNamespace
      new UID().getNamespace() mustEqual localUidNamespace

    }

  }

  "QualifiedNames" should {

    "be read from URI" in {

      QualifiedName.read(uri1) must haveClass[URI]
      QualifiedName.read(uri2).getNamespace() mustEqual "http://example.org/sample#"

    }

    "be read from UID" in {

      QualifiedName.read("xyz") must haveClass[UID]
      QualifiedName.read("xyz").getNamespace() mustEqual localUidNamespace

    }

    "be read from local namespaced UID" in {

      QualifiedName.read("local:uid:xyz") must haveClass[UID]
      QualifiedName.read("local:uid:xyz").getLocalName() mustEqual "xyz"

    }

  }

}
