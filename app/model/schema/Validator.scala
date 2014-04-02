package model.schema

import model.{Attribute, Resource}
import play.api.libs.json._
import scala.Some

trait Validator {

  def validate(resource: Resource) : ValidationResult

}

case class ValidationResult(violations: List[Violation]) {
  def isSuccess = violations.isEmpty
  def isError = !isSuccess
}

case class Violation(id: String, msg: String, decl: Option[PropertyDecl] = None, attr: Option[Attribute[Any]] = None)

object Violation {
  implicit val writeViolation = new Writes[Violation] {
    override def writes(v: Violation): JsValue = {
      v.decl.map(decl => Json.obj("property" -> decl)).getOrElse(Json.obj()) ++
      v.attr.map(attr => Json.obj("attribute" -> attr.name, "value" -> attr.valueAsString)).getOrElse(Json.obj()) ++
        Json.obj("errorId" -> v.id, "message" -> v.msg)
    }
  }
}

object ValidationResult {
  implicit val jsonWrites = new Writes[ValidationResult] {
    override def writes(vr: ValidationResult): JsValue = {
      if (vr.isSuccess)
        Json.obj("status" -> "ok")
      else
        Json.obj(
          "status" -> "error",
          "violations" -> vr.violations
        )
    }
  }
}

class SchemaValidator(schema: ResourceSchema) extends Validator {

  val propMap: Map[String, PropertyDecl] = schema.properties.map(decl => (decl.name, decl)).toMap

  override def validate(resource: Resource): ValidationResult = {

    val unmatchedMandatories: List[PropertyDecl] = schema.properties.filter(
      decl => resource.attributeOption(decl.name).isEmpty && decl.cardinality.min > 0
    )

    val undeclaredAttributes = resource.attributes().filterNot {
      attr =>
        propMap.get(attr.name).isDefined
    }

    val violations: List[Violation] = (
        unmatchedMandatories.map(errorMandatory)
          ++
        undeclaredAttributes.map(errorUndeclared)
          ++
        typeViolations(resource.attributes())
    )

    if (violations.isEmpty)
      success()
    else
      error(violations)
  }

  def success() = ValidationResult(List())

  def error(violations: List[Violation]) = ValidationResult(violations)

  // Violation detectors

  def typeViolations(attributes: Seq[Attribute[Any]]): List[Violation] = {
    List()
  }

  // Violation builders
  def errorMandatory(decl: PropertyDecl) =
    Violation("mandatory", s"Field '${decl.name}' is mandatory.", Some(decl))
  def errorUndeclared(attr: Attribute[Any]) =
    Violation("undeclared", s"No declaration found for attribute '${attr.name}'", None, Some(attr))

}
