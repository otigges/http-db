package controllers

import storage.mock.MockSchemaStore
import play.api.Logger
import play.api.mvc.Action
import play.api.libs.json.{JsObject, Json}
import model.structure.Document
import model.{QualifiedName, Resource}
import model.schema.ResourceSchema

object SchemaController extends BaseController {

  val schemaStore = MockSchemaStore

  def index() = Action {
    implicit request =>
      val schemas: Seq[ResourceSchema] = schemaStore.all()
      println(s"Schemas: ${schemas}")
      render {
        case Accepts.Json() =>  Ok(Json.toJson(Document(Map(), Map(), Json.obj("schemas" -> schemas))))
      }
  }

  def get(uid: String) = Action {
    implicit request =>
      val schema = schemaStore.findSchema(QualifiedName.read(uid))
      schema.map {
        s => render {
          case Accepts.Json() =>  Ok(Json.toJson(Document(Map(), Map(), Json.toJson(s).as[JsObject])))
        }
      }.getOrElse(NotFound(s"Found no schema for type ${uid}"))

  }

  def post() = play.mvc.Results.TODO

  def put(uid: String) = play.mvc.Results.TODO

}
