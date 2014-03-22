package model

import java.util.UUID
import play.api.libs.json.{JsString, JsValue, Writes}

trait QualifiedName {

  def getNamespace() : String
  def getLocalName() : String
  def qualified() : String
  def id() : String

  override def toString() : String = qualified()

}

object QualifiedName {

  val LOCAL_NAMESPACE = "local:uid:"

  implicit def read(in: String) : QualifiedName = {
    if (in.startsWith(LOCAL_NAMESPACE))
      new UID(in.substring(in.lastIndexOf(':') + 1))
    else if (in.contains("/"))
      new URI(in)
    else
      new UID(in)
  }

  implicit def write(in: QualifiedName) : String = in.id()

  implicit val writeJson = new Writes[QualifiedName] {
    override def writes(o: QualifiedName): JsValue = JsString(o.id())
  }

}


case class URI(uri: String) extends QualifiedName {

  def getNamespace(): String = {
    delim() match {
      case -1 => ""
      case i  => uri.substring(0, i +1)
    }
  }

  def getLocalName(): String = delim() match {
    case -1 => ""
    case i  => uri.substring(i +1)
  }

  def qualified(): String = uri

  def id(): String = uri

  private def delim() : Int = Math.max(uri.lastIndexOf('/'), uri.lastIndexOf('#'))

}

case class UID(uid: String = UUID.randomUUID().toString) extends QualifiedName {

  def getNamespace(): String = QualifiedName.LOCAL_NAMESPACE

  def getLocalName(): String = uid

  def qualified(): String = getNamespace() + uid

  def id(): String = uid

}

