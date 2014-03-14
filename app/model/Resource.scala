package model

import play.api.libs.json.{Json, JsValue, Writes}

/**
 *
 */
trait Resource {

  def qn() : QualifiedName

  def attributes() : Map[String, AnyRef]

  def associations() : Seq[Association]

}

object Resource {
  implicit val writeJson = new Writes[Resource] {
    def writes(r: Resource): JsValue = {
      Json.obj(
        "id" -> r.qn().id()
        //"attributes" -> r.attributes()
        //"associations" -> r.associations()
      )
    }
  }
}

class GenericResource(qualifiedName: QualifiedName = new UID(),
  attr : Map[String, AnyRef] = Map(),
  assocs : Seq[Association] = List())
  extends Resource
{

  def qn(): QualifiedName = qualifiedName

  def attributes(): Map[String, AnyRef] = attr

  def associations() = assocs

}
