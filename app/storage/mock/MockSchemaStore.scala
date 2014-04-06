package storage.mock

import scala.collection.mutable.{HashMap => MMap}

import storage.base.SchemaStore
import model.schema._
import model.{UID, QualifiedName}
import model.UID

/**
 * For Testing.
 */
object MockSchemaStore extends SchemaStore {

  private val memStore = new MMap[QualifiedName, ResourceSchema]()

  addMFixtures()

  //-------------------------------------------------------

  override def all() = memStore.values.toSeq

  override def findSchema(qn : QualifiedName) : Option[ResourceSchema] = memStore.get(qn)

  override def store(schema: ResourceSchema) : ResourceSchema = {
    memStore.put(schema.resourceType, schema)
    schema
  }

  //-------------------------------------------------------

  def clear() = memStore.clear()

  def addMFixtures() = {
    store(ResourceSchema(UID("Person"), List(
      PropertyDecl("name", DataType.Text, Cardinality.exactlyOne),
      PropertyDecl("favoriteSong", DataType.Text, Cardinality.zeroOrOne),
      PropertyDecl("address", DataType.Complex, Cardinality.zeroOrOne, List(new ComplexAttributeConstraint(List(
        PropertyDecl("street", DataType.Text, Cardinality.exactlyOne),
        PropertyDecl("zip", DataType.Number, Cardinality.exactlyOne),
        PropertyDecl("city", DataType.Text, Cardinality.exactlyOne)
      ))))
      )
    ))
    store(ResourceSchema(UID("Organization"), List(PropertyDecl("name", DataType.Text))))
  }

}
