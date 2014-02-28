package services.schema

import scala.collection.mutable


/**
 * Schema for resources.
 */
class ResourceSchema {

  val uid = java.util.UUID.randomUUID.toString
  var properties = mutable.Buffer.empty[Property]

  def add(property: Property) : ResourceSchema = {
    properties += property
    this
  }

}

case class Property(uid: String, name: String, dataType: Enumeration)

object Type extends Enumeration {
  type Type = Value
  val STRING, INTEGER, DECIMAL = Value
}
