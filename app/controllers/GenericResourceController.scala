package controllers

import play.api.mvc._
import model._
import storage.mock.MockGraphAccess
import play.api.{Play, Logger}
import model.structure.Document
import play.api.libs.json._
import play.api.libs.json.JsObject
import storage.base.{ResourceResolver, AssociationManager}

object GenericResourceController extends BaseController {

  val graphAccess = MockGraphAccess

  implicit val resolver = new ResourceResolver {
    override def resolve(qn: QualifiedName): Resource =
      graphAccess.findResource(qn).getOrElse {
        graphAccess.store(new GenericResource(qn))
      }
  }

  protected def resourceBaseUrl(implicit request: Request[_]) = baseUrl + "/resources"

  //-------------------------------------------------------

  def getGeneric(uid: String) = Action {
    implicit request =>
      Logger.info(s"Requesting resource ${uid}")
      graphAccess.findResource(uid).map {
        r =>
          render {
            case Accepts.Json() => Ok(Json.toJson(Document(Map(), Map(), Resource.toJson(resourceBaseUrl, r))))
          }
      }.getOrElse(NotFound(s"Found no resource with UID ${uid}"))
  }

  def getLinksGeneric(uid: String) = play.mvc.Results.TODO

  def putGeneric(uid: String) = Action {
    implicit request =>
      withJsonBody {
        body =>
         val resource = GenericResource.fromJson(uid, body.as[JsObject])
          graphAccess.store(resource)
         Logger.info(s"Stored resource ${resource.qn()}")
         NoContent
      }.getOrElse(BadRequest("No valid JSON body."))
  }

  def postLinksGeneric(uid: String) = Action {
    implicit request =>
      withJsonBody {
        body =>

        val links: Seq[Link] = body match {
          case JsArray(jsLinks) => jsLinks.map(Link.fromJson)
          case _ => Seq(Link.fromJson(body))
        }

        graphAccess.findResource(QualifiedName.read(uid)).map {
          subject =>
            if (links.isEmpty)
              NoContent
            else {
              val resource: Resource = new AssociationManager().addLinks(subject, links: _*)
              graphAccess.store(resource)
              Logger.info(s"Updated links of resource ${resource.qn()} : ${resource.associations()}")
              Created
            }
        }.getOrElse(NotFound)

      }.getOrElse(BadRequest("No valid JSON body."))
  }

}
