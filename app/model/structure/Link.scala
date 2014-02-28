package model.structure

import play.api.libs.json.Writes
import play.api.libs.json.JsValue
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.functional.syntax._
import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.mvc.Call
import play.GlobalSettings
import play.api.Play

case class Link(href: String,
  templated: Boolean = false,
  typeHint: String = "",
  deprecation: String = "",
  name: String = "",
  profile: String = "",
  title: String = "",
  hreflang: String = "")


object Link {

  val context = Play.current.configuration.getString("http.context").getOrElse("")
  
  implicit val writesLinks =
    ((__ \ "href").write[String] and
      nullable[Boolean](__ \ "templated") and
      nullable[String] (__ \ "typeHint") and
      nullable[String] (__ \ "deprecation") and
      nullable[String] (__ \ "name") and
      nullable[String](__ \ "profile") and
      nullable[String](__ \ "title") and
      nullable[String](__ \ "hreflang"))(unlift(Link.unapply))

  def baseUrl(implicit request: Request[_]) = {
    s"http://${request.host}${context}"
  }

  /**
   * A selfLink constructed from the given request path
   */
  def selfLink(implicit request: Request[_]) = {
    "self" -> Link(baseUrl + request.uri)
  }
  
  def link (path : String) (implicit request: Request[AnyContent]) : Link= {
    Link (baseUrl + path)
  }
  
  def link (call : Call) (implicit request: Request[AnyContent]) : Link= 
    link ( call.url)

  def template (path : String) (implicit request: Request[AnyContent]) : Link= {
    Link (baseUrl + path, true)
  }

  private def nullable[A](path: JsPath)(implicit wrs: Writes[A]): OWrites[A]  = OWrites[A] { a =>
    a match {
      case ""|null => Json.obj()
      case a => JsPath.createObj(path -> wrs.writes(a))
    }
  }
  
}

