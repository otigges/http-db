package storage.mock

import scala.collection.mutable.{HashMap => MMap}
import model._
import model.UID
import storage.base.GraphAccess

/**
 * For testing.
 */
object MockGraphAccess extends GraphAccess {

  private val memStore = new MMap[QualifiedName, Resource]()

  // ------------------------------------------------------

  def findResource(qn : QualifiedName) : Option[Resource] = memStore.get(qn)

  def store(res : Resource) = { memStore.put(res.qn(), res); res}

  def delete(qn : QualifiedName) = memStore.remove(qn)

  // ------------------------------------------------------

  def clear() = memStore.clear()

  def addFixtures() = {
    store(new GenericResource(UID("JohnDoe")))
    store(new GenericResource(URI("http://www.w3.org/1999/02/22-rdf-syntax-ns#type")))
  }

}
