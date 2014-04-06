package model.schema

import model.{Attribute, UID, QualifiedName}
import play.api.libs.json._
import play.api.libs.json.JsArray
import scala.collection.Set

case class ResourceSchema(resourceType: QualifiedName, properties : List[PropertyDecl] = List()) {

}

object ResourceSchema {

  def toJson(rs: ResourceSchema) : JsObject = {
    Json.obj(
      "describes" -> rs.resourceType,
      "properties" -> JsArray(
        rs.properties.map {
          p => Json.toJson(p)
        }
      )
    )
  }

  def fromJson(uid: String, json: JsValue) : ResourceSchema = {
    val describes: Option[String] = (json \ "describes").asOpt[String]
    describes.map(d => if (d != uid) throw new IllegalStateException(s"Given type ${describes} does not correspond to expected type ${uid}."))
    build(uid, json)
  }

  def fromJson(json: JsValue) : ResourceSchema = {
    val describes: String = (json \ "describes").as[String]
    build(describes, json)
  }

  private def build(uid: String, json: JsValue) = {
    val decls: Seq[PropertyDecl] = (json \ "properties").as[JsArray].value.map(js => PropertyDecl.fromJson(js))
    new ResourceSchema(uid, decls.toList)
  }
}
