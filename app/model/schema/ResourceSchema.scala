package model.schema

import model.QualifiedName
import play.api.libs.json.{JsArray, Json, JsValue, Writes}

case class ResourceSchema(resourceType: QualifiedName, properties : List[PropertyDecl] = List()) {

}

object ResourceSchema {
  implicit val writeJson = new Writes[ResourceSchema] {
    override def writes(rs: ResourceSchema): JsValue = {
      Json.obj(
        "type" -> rs.resourceType,
        "properties" -> JsArray(
          rs.properties.map {
            p => Json.toJson(p)
          }
        )
      )
    }
  }
}
