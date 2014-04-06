package model

import play.api.libs.json._
import model.AttrType.AttrType

object AttrType extends Enumeration {
  type AttrType = Value
  val Text, Number, Boolean, Sequence, Complex, Null = Value
}

trait Attribute[+T] {

  def name : String

  def value : T

  def attrType : AttrType

  def valueAsString : String

  def valueDefinition : Value[T]

}

trait Value[+T] {

  def value : T

  def attrType : AttrType

  def valueAsString : String

}

case class TextValue(value: String) extends Value[String]{

  override def valueAsString: String = value

  override def attrType: AttrType = AttrType.Text

}

case class NumberValue(value: BigDecimal) extends Value[BigDecimal]{

  override def valueAsString: String = value.toString()

  override def attrType: AttrType = AttrType.Number

}

case class BooleanValue(value: Boolean) extends Value[Boolean]{

  override def valueAsString: String = value.toString

  override def attrType: AttrType = AttrType.Boolean

}

object NullValue extends Value[Any]{

  override def valueAsString: String = value.toString

  override def attrType: AttrType = AttrType.Null

  override def value: Any = null
}

case class ComplexValue(value: Seq[Attribute[Any]]) extends Value[Seq[Attribute[Any]]]{

  override def valueAsString: String = value.toString

  override def attrType: AttrType = AttrType.Complex
}

case class SeqValue(value: Seq[Value[Any]]) extends Value[Seq[Value[Any]]]{

  override def valueAsString: String = value.toString

  override def attrType: AttrType = AttrType.Sequence

}

class SimpleAttribute[T](attrName: String, attrValue: Value[T]) extends Attribute[T] {

  def name: String = attrName

  def value: T = attrValue.value

  def attrType: AttrType = attrValue.attrType

  def valueAsString: String = attrValue.toString

  def valueDefinition : Value[T] = attrValue

  override def toString = attrName + "=" + attrValue

}

class TextAttribute(attrName: String, attrValue: String)
  extends SimpleAttribute[String](attrName, TextValue(attrValue))

class NumberAttribute(attrName: String, attrValue: BigDecimal)
  extends SimpleAttribute[BigDecimal](attrName, NumberValue(attrValue))

class BooleanAttribute(attrName: String, attrValue: Boolean)
  extends SimpleAttribute[Boolean](attrName, BooleanValue(attrValue))

class NullAttribute(attrName: String)
  extends SimpleAttribute[Any](attrName, NullValue) {

  override def toString = attrName + "=null"

}

class SeqAttribute(attrName: String, attrValue: Seq[Value[Any]]) extends Attribute[Seq[Value[Any]]] {

  override def name: String = attrName

  override def value: Seq[Value[Any]] = attrValue

  override def attrType: AttrType = AttrType.Sequence

  override def valueAsString: String = attrValue.toString()

  override def valueDefinition: Value[Seq[Value[Any]]] = SeqValue(attrValue)

}

class ComplexAttribute(attrName: String, attrValue: Seq[Attribute[Any]]) extends Attribute[Seq[Attribute[Any]]] {

  override def name: String = attrName

  override def value: Seq[Attribute[Any]] = attrValue

  override def attrType: AttrType = AttrType.Complex

  override def valueAsString: String = attrValue.toString()

  override def valueDefinition: Value[Seq[Attribute[Any]]] = ComplexValue(attrValue)
}


object Attribute {
  implicit val jsonWrites = new Writes[Attribute[Any]] {
    override def writes(a: Attribute[Any]): JsValue = {
      Json.obj(a.name -> Value.toJson(a.valueDefinition))
    }
  }

  def fromJson(attr: (String, JsValue)) : Attribute[Any] = {
    val name = attr._1
    val value: JsValue = attr._2

    value match {
      case JsString(x) => new TextAttribute(name, x)
      case JsNumber(x) => new NumberAttribute(name, x)
      case JsBoolean(x) => new BooleanAttribute(name, x)
      case JsObject(xs) => new ComplexAttribute(name, xs.map(fromJson))
      case JsArray(xs) => new SeqAttribute(name, xs.map(Value.fromJson))
      case JsNull => new NullAttribute(name)
      case _ => throw new RuntimeException("Invalid json type: " + value)
    }
  }
}

object Value {
  implicit val jsonWrites = new Writes[Value[Any]] {
    override def writes(v: Value[Any]): JsValue = Value.toJson(v)
  }

  def toJson(v: Value[Any]): JsValue = v.attrType match {
    case AttrType.Text => JsString(v.valueAsString)
    case AttrType.Number => JsNumber(v.value.asInstanceOf[BigDecimal])
    case AttrType.Boolean => JsBoolean(v.value.asInstanceOf[Boolean])
    case AttrType.Complex => v.value.asInstanceOf[Seq[Attribute[Any]]].
      foldLeft ( Json.obj() ) ((a,b) => a ++ Json.toJson(b).as[JsObject])
    case AttrType.Sequence => JsArray(v.value.asInstanceOf[Seq[Value[Any]]].map(toJson))
    case AttrType.Null => JsNull
  }

  def fromJson(value: JsValue) : Value[Any] = {
    value match {
      case JsString(x) => TextValue(x)
      case JsNumber(x) => NumberValue(x)
      case JsBoolean(x) => BooleanValue(x)
      case JsObject(xs) => ComplexValue(xs.map(Attribute.fromJson))
      case JsArray(xs) => SeqValue(xs.map(fromJson))
      case JsNull => NullValue
      case _ => throw new RuntimeException("Invalid json type: " + value)
    }
  }

  def isComplex(v: Value[Any]) = v.isInstanceOf[ComplexValue]
  def isSequence(v: Value[Any]) = v.attrType == AttrType.Sequence
  def isObject(v: Value[Any]) = v.attrType == AttrType.Sequence

}





