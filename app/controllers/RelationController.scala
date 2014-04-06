package controllers

import storage.mock.MockSchemaStore
import play.api.Logger
import play.api.mvc.{Request, Action}
import play.api.libs.json.{JsObject, Json}
import model.structure.{DocLink, Document}
import model.{QualifiedName, Resource}
import model.schema.ResourceSchema

object RelationController extends BaseController {

  def index() = TODO

  def get(uid : String)  = TODO

}
