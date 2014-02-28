package model

import play.api.libs.json.{Json, JsValue, Writes}

/**
 *
 */
trait Resource {

  def qn() : QualifiedName

  def attributes() : Map[String, AnyRef]

}

object Resource {
  implicit val writeJson = new Writes[Resource] {
    def writes(r: Resource): JsValue = {
      Json.obj(
        "id" -> r.qn().id()
      )
    }
  }
}

class GenericResource(qualifiedName: QualifiedName = new UID(), attr : Map[String, AnyRef] = Map()) extends Resource {
  def qn(): QualifiedName = qualifiedName
  def attributes(): Map[String, AnyRef] = attr
}
