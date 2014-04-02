package controllers

import play.api.mvc._
import model._
import storage.mock.{MockSchemaStore, MockGraphAccess}
import play.api.{Play, Logger}
import model.structure.Document
import play.api.libs.json._
import play.api.libs.json.JsObject
import storage.base.{ResourceResolver, AssociationManager}
import model.schema.{ValidationResult, SchemaValidator, ResourceSchema}

object TypedResourceController extends BaseController {

  val graphAccess = MockGraphAccess

  val schemaStore = MockSchemaStore

  implicit val resolver = new ResourceResolver {
    override def resolve(qn: QualifiedName): Resource =
      graphAccess.findResource(qn).getOrElse {
        graphAccess.store(new GenericResource(qn))
      }
  }

  protected def resourceBaseUrl(implicit request: Request[_]) = baseUrl + "/resources"

  //-------------------------------------------------------

  def index() = Action { implicit request =>
    val map: Map[String, String] = schemaStore.all().map {
      schema =>
        val uid = schema.resourceType.id()
        (uid, baseUrl + routes.TypedResourceController.getAll(uid).url)
    }.toMap

    Ok(Json.toJson(Document(Map(), Map(), Json.obj("entities" -> map))))

  }

  def getAll(resourceType: String) = Action {
    schemaStore.findSchema(QualifiedName.read(resourceType)).map {
      schema =>
        Ok(Json.toJson(schema))
    }.getOrElse(NotFound("ResourceType/Schema not found"))
  }

  def get(resourceType: String, uid: String) = Action {
    implicit request =>
      Logger.info(s"Requesting resource ${uid} of type ${resourceType}")

      lookup(resourceType, uid) match {
        case Right(r) =>
          render {
            case Accepts.Json() => Ok(Json.toJson(Document(Map(), Map(), Resource.toJson(resourceBaseUrl, r))))
          }
        case Left(s) => NotFound(s)
      }
  }

  def getLinks(resourceType: String, uid: String) = play.mvc.Results.TODO

  def post(resourceType: String) = Action {
    implicit request =>
      schema(resourceType).map {
        schema =>
          withJsonBody {
            json =>
              val resource = GenericResource.fromJson(UID(), json.as[JsObject])
              val result: ValidationResult = new SchemaValidator(schema).validate(resource)
              if (result.isSuccess) {
                // TODO: set type
                graphAccess.store(resource)
                Created.withHeaders("Location" -> location(resourceType, resource.qn().id()))
              } else {
                Conflict(Json.toJson(Document(Map(), Map(), Json.toJson(result).as[JsObject])))
              }
          }.getOrElse(BadRequest("No valid JSON body."))
      }.getOrElse(NotFound("Schema not found."))
  }

  //-------------------------------------------------------

  private def lookup(resourceType: String, uid: String): Either[String, TypedResource] = {
    schema(resourceType).map {
      schema =>
        resource(uid).map {
          resource =>
            Right(new TypedResource(resource, schema))
        }.getOrElse(Left("Resource Not Found"))
    }.getOrElse(Left("Schema not found"))
  }

  private def schema(sid: String): Option[ResourceSchema] = schemaStore.findSchema(QualifiedName.read(sid))

  private def resource(rid: String): Option[Resource] = graphAccess.findResource(QualifiedName.read(rid))

  private def location(resourceType: String, uid: String)(implicit request: Request[Any]) : String =
    routes.TypedResourceController.get(resourceType, uid).url


}
