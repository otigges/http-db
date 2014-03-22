package controllers

import play.api.mvc.Action
import play.api.libs.json.Json
import model.structure.{DocLink, Document}

/**
 * Home document.
 */
object HomeController extends BaseController {

  def index() = Action {
    implicit request =>
      Ok(Json.toJson(
        Document(Map(
          DocLink.selfLink,
          "schemas" -> DocLink(baseUrl + routes.SchemaController.index().url)
        ), Map())
      ))
  }

}
