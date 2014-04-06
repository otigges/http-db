package model.schema

import play.api.libs.json._
import play.api.libs.json.JsString
import model._
import model.ComplexValue
import play.api.Logger


object DataType extends Enumeration {
  val Resource, Text, Number, Boolean, Complex = Value
}

object ConstrainType extends Enumeration {
  val InnerSchema, Pattern, Range = Value

  def propName(v: Value) = v match {
    case InnerSchema => "properties"
    case Pattern => "pattern"
    case Range => "range"
    case _ => throw new IllegalArgumentException(s"Unknown constraint type: ${v}")
  }
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

  def parse(s: String) : Cardinality = {
    Logger.warn("Cardinality Parsing not yet Implemented")
    unbound
  }
}

/**
 * Declaration of a Property - either resource reference or value - of a resource.
 */
case class PropertyDecl(name: String,
                        dataType: DataType.Value,
                        cardinality: Cardinality = Cardinality.unbound,
                        constraints: List[Constraint] = List()) {

  def toValue : Value[Any] = {
    ComplexValue(Seq(
      new TextAttribute("name",  name),
      new TextAttribute("type",  dataType.toString),
      new TextAttribute("cardinality",  cardinality.toString)
      ) ++ constraints.map( c => new SimpleAttribute(ConstrainType.propName(c.constraintType), c.toValue))
    )
  }

}

object PropertyDecl {
  implicit val writeJson = new Writes[PropertyDecl] {
    override def writes(p: PropertyDecl): JsValue = Json.toJson(p.toValue)
  }

  def fromJson(js : JsValue) : PropertyDecl = {
    val name: String = (js \ "name").as[String]
    val dataType: String = (js \ "type").as[String]
    val cardinality: String = (js \ "cardinality").as[String]

    new PropertyDecl(name, DataType.withName(dataType), Cardinality.parse(cardinality))
  }
}

trait Constraint {

  def validate(attr : Attribute[Any]) : List[Violation]

  def constraintType : ConstrainType.Value

  def toValue : Value[Any]

}

case class ComplexAttributeConstraint(properties : List[PropertyDecl] = List()) extends Constraint {

  override def validate(attr: Attribute[Any]): List[Violation] = {

    new SchemaValidator(properties).validate(attr.asInstanceOf[Attribute[Seq[Attribute[Any]]]].value).violations

  }

  override def constraintType = ConstrainType.InnerSchema

  override def toValue: Value[Any] = SeqValue(properties.map(_.toValue))

}




