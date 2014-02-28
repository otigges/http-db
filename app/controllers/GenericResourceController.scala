package controllers

import play.api.mvc._
import model._
import storage.mock.MockGraphAccess
import play.api.Logger
import model.structure.Document
import play.api.libs.json._
import play.api.libs.json.JsObject
import scala.collection

object GenericResourceController extends Controller {

  def getGeneric(uid: String) = Action {
    implicit request =>
      Logger.info(s"Requesting resource ${uid}")
      MockGraphAccess.findResource(uid).map {
        r =>
          render {
            case Accepts.Json() => Ok(Json.toJson(Document(Map(), Map(), Json.toJson(r).as[JsObject])))
          }
      }.getOrElse(NotFound(s"Found no resource with UID ${uid}"))
  }

  def get(resourceType: String, uid: String) = play.mvc.Results.TODO

  def getLinksGeneric(uid: String) = play.mvc.Results.TODO

  def getLinks(resourceType: String, uid: String) = play.mvc.Results.TODO

  def putGeneric(uid: String) = Action {
    request =>
      val jsonBody: Option[JsValue] = request.body.asJson
      jsonBody.map{
        body =>
         val resource = parseGenericResource(uid, body.as[JsObject])
         MockGraphAccess.store(resource)
         Logger.info(s"Stored resource ${resource.qn()}")
         NoContent
      }.getOrElse(BadRequest("No valid JSON body."))
  }

  // ------------------------------------------------------

  private def parseGenericResource(uid: String, json: JsObject) : GenericResource = {
    val attributes: collection.Map[String, AnyRef] = json.value.mapValues[AnyRef](jsVal => jsVal.toString()).filterKeys(k => "uid".equals(k) )
    new GenericResource(QualifiedName.read(uid), attributes.toMap)
  }

}
