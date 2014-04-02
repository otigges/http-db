package model

import play.api.libs.json._
import scala.collection.Set
import model.schema.ResourceSchema

trait Resource {

  def qn() : QualifiedName

  def attributes() : Seq[Attribute[Any]]

  def attribute(name: String) : Attribute[Any] = attributeOption(name).getOrElse(null)

  def attributeOption(name: String) : Option[Attribute[Any]] = attributes().find(p => p.name == name)

  def associations() : Set[Association]

  def ++(assocs: Association*) : Resource

  override def toString() = qn().id()

}

object Resource {

  def toJson(uriBase: String, r: Resource): JsObject = {
      val id = Json.obj("id" -> r.qn().id())
      val obj = r.attributes().foldLeft(id)( (acc, att) => acc ++ Json.toJson(att).as[JsObject])
      obj ++ Json.obj("associations" -> r.associations().map {
        assoc => Link.toJson(uriBase, assoc.toLink)
      })
    }

}

class GenericResource(qualifiedName: QualifiedName = new UID(),
  attr : Seq[Attribute[Any]] = List(),
  assocs : Set[Association] = Set())
  extends Resource
{

  def qn(): QualifiedName = qualifiedName

  def associations() = assocs

  override def attributes() = attr

  override def ++(associations: Association*) = {
    new GenericResource(qualifiedName, attr, this.assocs ++ associations )
  }
}

object GenericResource {

  def fromJson(uid: String, json: JsObject) : GenericResource = {

    def valueFromJson(value: JsValue) : Value[Any] = {

      value match {
        case JsString(x) => TextValue(x)
        case JsNumber(x) => NumberValue(x)
        case JsBoolean(x) => BooleanValue(x)
        case JsObject(xs) => ComplexValue(xs.map(attrFromJson), AttrType.Complex)
        case JsArray(xs) => ComplexValue(xs.map(valueFromJson), AttrType.Sequence)
        case JsNull => NullValue
        case _ => throw new RuntimeException("Invalid json type: " + value)
      }
    }

    def attrFromJson(attr: (String, JsValue)) : Attribute[Any] = {
      val name = attr._1
      val value: JsValue = attr._2

      value match {
        case JsString(x) => new TextAttribute(name, x)
        case JsNumber(x) => new NumberAttribute(name, x)
        case JsBoolean(x) => new BooleanAttribute(name, x)
        case JsObject(xs) => new ComplexAttribute(name, xs.map(attrFromJson))
        case JsArray(xs) => new SeqAttribute(name, xs.map(valueFromJson))
        case JsNull => new NullAttribute(name)
        case _ => throw new RuntimeException("Invalid json type: " + value)
      }
    }

    val attributes: Set[Attribute[Any]] = json.fieldSet.filterNot( f => f._1.trim == "id").map(attrFromJson)

    new GenericResource(QualifiedName.read(uid), attributes.toSeq)
  }

}

class TypedResource(resource: Resource, schema: ResourceSchema)
  extends Resource
{

  override def qn(): QualifiedName = resource.qn()

  override def associations(): Set[Association] = resource.associations()

  override def attributes(): Seq[Attribute[Any]] = {
    schema.properties.map (
      decl =>
        resource.attributeOption(decl.name).getOrElse(new NullAttribute(decl.name))
    )
  }

  override def ++(assocs: Association*): Resource = {
    new TypedResource(resource ++ (assocs: _*), schema)
  }




}
