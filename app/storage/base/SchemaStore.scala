package storage.base

import model.QualifiedName
import model.schema.ResourceSchema

trait SchemaStore {

  def all() : Seq[ResourceSchema]

  def findSchema(qn : QualifiedName) : Option[ResourceSchema]

  def store(schema: ResourceSchema) : ResourceSchema

}
