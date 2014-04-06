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

    val attributes: Set[Attribute[Any]] = json.fieldSet.filterNot( f => f._1.trim == "id").map(Attribute.fromJson)

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
