package storage.mock

import play.api.Logger

import scala.collection.mutable.{HashMap => MMap}
import model._
import model.UID
import storage.base.GraphAccess

/**
 * For testing.
 */
object MockGraphAccess extends GraphAccess {

  private val memStore = new MMap[QualifiedName, Resource]()

  addFixtures()

  // ------------------------------------------------------

  def findResource(qn : QualifiedName) : Option[Resource] = memStore.get(qn)

  def store(res : Resource) = { memStore.put(res.qn(), res); res}

  def delete(qn : QualifiedName) = memStore.remove(qn)

  def findByType(expected: QualifiedName) : Iterable[Resource] = {
    memStore.values.filter( isOfType(_, expected))
  }

  // ------------------------------------------------------

  def clear() = memStore.clear()

  def addFixtures() = {
    val acme: Resource = store(new GenericResource(UID("AcmeInc."), Seq(
      new TextAttribute("companyName", "Acme Inc."),
      new TextAttribute("type", "Organization")))
    )
    val john: Resource = store(new GenericResource(UID("JohnDoe"), Seq(
      new TextAttribute("firstName", "John"),
      new TextAttribute("lastName", "Doe"),
      new TextAttribute("type", "Person")))
    )
    store(john ++ Association(john, new GenericResource(UID("worksFor")), acme))
    store(new GenericResource(URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")))
  }

  private def isOfType(resource: Resource, expectedType: QualifiedName) : Boolean = {
    resource.attributeOption("type").exists {
      actual : Attribute[Any] =>
        val actualType = actual.valueAsString
        Logger.info(s"Actual type: ${actualType}")
        actualType == expectedType.getLocalName() || actualType == expectedType.id()
      }
  }

}
