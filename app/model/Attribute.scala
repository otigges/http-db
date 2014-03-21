package model

import play.api.libs.json._
import model.AttrType.AttrType

object AttrType extends Enumeration {
  type AttrType = Value
  val Text, Number, Boolean = Value
}

trait Attribute[+T] {

  def name() : String

  def value() : T

  def attrType() : AttrType

  def valueAsString() : String

}

class TextAttribute(attrName: String, attrValue: String) extends Attribute[String] {

  def name(): String = attrName

  def value(): String = attrValue

  def valueAsString(): String = attrValue

  def attrType() = AttrType.Text

  override def toString() = attrName + "=" + attrValue

}

class NumberAttribute(attrName: String, attrValue: BigDecimal) extends Attribute[BigDecimal] {

  def name(): String = attrName

  def value(): BigDecimal = attrValue

  def valueAsString(): String = attrValue.toString

  def attrType() = AttrType.Number

  override def toString() = attrName + "=" + attrValue

}

class BooleanAttribute(attrName: String, attrValue: Boolean) extends Attribute[Boolean] {

  def name(): String = attrName

  def value(): Boolean = attrValue

  def valueAsString(): String = attrValue.toString

  def attrType() = AttrType.Boolean

  override def toString() = attrName + "=" + attrValue

}

object Attribute {
  implicit val jsonWrites = new Writes[Attribute[Any]] {
    override def writes(a: Attribute[Any]): JsValue = {
      a.attrType match {
        case AttrType.Text => Json.obj(a.name() -> a.valueAsString())
        case AttrType.Number => Json.obj(a.name() -> a.value.asInstanceOf[BigDecimal])
        case AttrType.Boolean => Json.obj(a.name() -> a.value.asInstanceOf[Boolean])
      }
    }

  }
}
