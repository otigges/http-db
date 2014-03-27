package controllers

import play.api.mvc._
import model._
import storage.mock.{MockSchemaStore, MockGraphAccess}
import play.api.{Play, Logger}
import model.structure.Document
import play.api.libs.json._
import play.api.libs.json.JsObject
import storage.base.{ResourceResolver, AssociationManager}
import model.schema.ResourceSchema

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

  def get(resourceType: String, uid: String) = Action {
    implicit request =>
      Logger.info(s"Requesting resource ${uid} of type ${resourceType}")

      lookup(resourceType, uid) match {
        case Right(r) =>
          render {
            case Accepts.Json() => Ok(Json.toJson(Document(Map(), Map(), Resource.toJson(resourceBaseUrl, r._1))))
          }
        case Left(s) => NotFound(s)
      }
  }

  def getLinks(resourceType: String, uid: String) = play.mvc.Results.TODO

  //-------------------------------------------------------

  private def lookup(resourceType: String, uid: String): Either[String, (Resource, ResourceSchema)] = {
    schema(resourceType).map {
      schema =>
        resource(uid).map {
          resource =>
            Right(resource, schema)
        }.getOrElse(Left("Resource Not Found"))
    }.getOrElse(Left("Schema not found"))
  }

  private def schema(sid: String): Option[ResourceSchema] = schemaStore.findSchema(QualifiedName.read(sid))

  private def resource(rid: String): Option[Resource] = graphAccess.findResource(QualifiedName.read(rid))


}
