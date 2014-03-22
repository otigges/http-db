package model

import play.api.libs.json._
import scala.collection.Set

trait Resource {

  def qn() : QualifiedName

  def attributes() : Seq[Attribute[Any]]

  def attribute(name: String) : Attribute[Any] = attributeOption(name).getOrElse(null)

  def attributeOption(name: String) : Option[Attribute[Any]] = attributes().find(p => p.name() == name)

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

    val attributes: Set[Attribute[Any]] = json.fieldSet.filterNot( f => f._1.trim == "id").map {
      attr =>
        val name = attr._1
        val value: JsValue = attr._2

        value match {
          case JsString(x) => new TextAttribute(name, x)
          case JsNumber(x) => new NumberAttribute(name, x)
          case JsBoolean(x) => new BooleanAttribute(name, x)
          case _ => throw new RuntimeException("Invalid json type")
        }
    }

    new GenericResource(QualifiedName.read(uid), attributes.toSeq)
  }

}
