package model.schema

import play.api.libs.json._
import play.api.libs.json.JsString


object DataType extends Enumeration {
  val Resource, Text, Number, Boolean, Complex = Value
}

case class Cardinality(min: Int = 0, max: Int = 1, unbound: Boolean = false) {
  override def toString = {
    min + ".." + (if (unbound) "*" else max)
  }
}

object Cardinality {
  def exactlyOne = Cardinality(1,1)
  def zeroOrOne = Cardinality(0,1)
  def unbound = Cardinality(0, Int.MaxValue, unbound = true)
}

/**
 * Declaration of a Property - either resource reference or value - of a resource.
 */
case class PropertyDecl(name: String,
                        dataType: DataType.Value,
                        cardinality: Cardinality = Cardinality.unbound,
                        constraints: List[String] = List())

object PropertyDecl {
  implicit val writeJson = new Writes[PropertyDecl] {
    override def writes(p: PropertyDecl): JsValue = Json.obj(
      "name" -> p.name,
      "dataType" -> p.dataType.toString,
      "cardinality" -> p.cardinality.toString
    )
  }
}

trait Constraint {

}

case class ComplexAttributeConstraint(properties : List[PropertyDecl] = List()) extends Constraint



