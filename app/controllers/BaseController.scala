package controllers

import play.api.mvc.{AnyContent, SimpleResult, Request, Controller}
import play.api.Play
import play.api.libs.json.JsValue

class BaseController extends Controller {

  val context = Play.current.configuration.getString("http.context").getOrElse("")

  protected def baseUrl(implicit request: Request[_]) = {
    s"http://${request.host}${context}"
  }

  // ------------------------------------------------------

  protected def withJsonBody(f: (JsValue => SimpleResult))(implicit request: Request[AnyContent]): Option[SimpleResult] = {
    request.body.asJson.map {
      body => f(body)
    }
  }

}
