package model

import play.api.libs.json._

/**
 * Represents an association between to resources.
 */
case class Association(source: Resource, predicate: Resource, target: Resource, context: Context = Unspecified) {

  def toLink = Link(predicate.qn(), target.qn(), context)

}

/**
 * Represents the link from a given resource to other resources.
 * TODO: Should links have an ID?
 */
case class Link(predicate: QualifiedName, target: QualifiedName, context: Context = Unspecified)

object Link {

  def toJson(uriBase: String, link: Link) = Json.obj(
      "predicate" -> link.predicate.id(),
      "resource" -> link.target.id(),
      "link" -> (uriBase + link.target.id())
  )

  def fromJson(json: JsValue) : Link = {
    val predicate = (json \ "predicate").as[String]
    val target = (json \ "resource").as[String]
    Link(QualifiedName.read(predicate), QualifiedName.read(target))
  }

}