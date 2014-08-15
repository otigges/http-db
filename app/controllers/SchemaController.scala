package controllers

import controllers.routes
import storage.mock.MockSchemaStore
import play.api.Logger
import play.api.mvc.{Request, Action}
import play.api.libs.json.{JsValue, JsArray, JsObject, Json}
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
          if (body.isInstanceOf[JsObject]) {
            val schema: ResourceSchema = ResourceSchema.fromJson(body)
            if (schemaStore.findSchema(schema.resourceType).isEmpty) {
              Logger.info(s"Creating schema: ${schema}")
              schemaStore.store(schema)
              Created.withHeaders("Location" -> location(schema))
            } else {
              Logger.info(s"Schema for type ${schema} already defined.")
              Conflict(s"Schema for type ${schema.resourceType} already defined.")
            }
          } else if (body.isInstanceOf[JsArray]) {
            val values: Seq[JsValue] = body.as[JsArray].value
            val schemas: Seq[ResourceSchema] = values.map( ResourceSchema.fromJson )
                .map (schemaStore.store (_))
            Logger.info(s"Stored Schemas: ${schemas.map(schema => schema.resourceType)}}")
            Accepted(s"Stored schemas: ${schemas.map(schema => schema.resourceType)} ")
          } else {
            BadRequest("Expected single JSON schema.")
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
    Map(
      DocLink.selfLink,
      "schemas" -> DocLink(baseUrl + "/schemas/{type}", true)
    )
  }

  private def links(schema: ResourceSchema)(implicit request: Request[Any]) = {
    Map(
      "self" -> DocLink(location(schema)),
      "instances" -> DocLink(baseUrl + routes.TypedResourceController.getAll(schema.resourceType.id()).url)
    )
  }

  private def location(schema : ResourceSchema)(implicit request: Request[Any]) : String =
    location(schema.resourceType.id())

  private def location(uid: String)(implicit request: Request[Any]) : String =
    baseUrl + routes.SchemaController.get(uid).url

}
