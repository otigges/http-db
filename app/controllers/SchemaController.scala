package controllers

import storage.mock.MockSchemaStore
import play.api.Logger
import play.api.mvc.{Request, Action}
import play.api.libs.json.{JsObject, Json}
import model.structure.{DocLink, Document}
import model.{QualifiedName, Resource}
import model.schema.ResourceSchema

object SchemaController extends BaseController {

  val schemaStore = MockSchemaStore

  def index() = Action {
    implicit request =>
      val schemas: Seq[ResourceSchema] = schemaStore.all()

      render {
        case Accepts.Json() =>  Ok(Json.toJson(Document(links(), Map(), Json.obj("schemas" -> schemas.map {
          schema => Json.obj("uri" -> location(schema)) ++ ResourceSchema.toJson(schema)
        }))))
      }
  }

  def get(uid: String) = Action {
    implicit request =>
      val schema = schemaStore.findSchema(QualifiedName.read(uid))
      schema.map {
        s => render {
          case Accepts.Json() => Ok(Json.toJson(Document(links(s), Map(), ResourceSchema.toJson(s))))
        }
      }.getOrElse(NotFound(s"Found no schema for type ${uid}"))

  }

  def post() = Action {
    implicit request =>
      withJsonBody {
        body =>
          val schema: ResourceSchema = ResourceSchema.fromJson(body)
          Logger.info(s"Creating schema: ${schema}")
          if (schemaStore.findSchema(schema.resourceType).isEmpty) {
            schemaStore.store(schema)
            Created.withHeaders("Location" -> location(schema))
          } else {
            Conflict(s"Schema for type ${schema.resourceType} already defined.")
          }
      }.getOrElse(BadRequest("No valid JSON."))
  }

  def put(uid: String) = Action {
    implicit request =>
      withJsonBody {
        body =>
          val schema: ResourceSchema = ResourceSchema.fromJson(uid, body)
          Logger.info(s"Updating schema: ${schema}")
          schemaStore.store(schema)
          NoContent
      }.getOrElse(BadRequest("No valid JSON."))
  }

  private def links()(implicit request: Request[Any]) = {
    Map(DocLink.selfLink, "schemas" -> DocLink(baseUrl + "/schemas/{type}", true))
  }

  private def links(schema: ResourceSchema)(implicit request: Request[Any]) = {
    Map("self" -> DocLink(location(schema)))
  }

  private def location(schema : ResourceSchema)(implicit request: Request[Any]) : String =
    location(schema.resourceType.id())

  private def location(uid: String)(implicit request: Request[Any]) : String =
    baseUrl + routes.SchemaController.get(uid).url


}
