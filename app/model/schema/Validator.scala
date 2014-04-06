package model.schema

import model.{Attribute, Resource}
import play.api.libs.json._
import scala.Some

trait Validator {

  def validate(attributes: Seq[Attribute[Any]]) : ValidationResult

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

class SchemaValidator(properties: List[PropertyDecl]) extends Validator {

  def this(schema: ResourceSchema) = this(schema.properties)

  val propMap: Map[String, PropertyDecl] = properties.map(decl => (decl.name, decl)).toMap

  def validate(resource: Resource): ValidationResult = {
    validate(resource.attributes())
  }

  override def validate(attributes: Seq[Attribute[Any]]): ValidationResult = {

    val unmatchedMandatories: List[PropertyDecl] = properties.filter(
      decl => !attributes.exists(a => a.name == decl.name) && decl.cardinality.min > 0
    )

    val undeclaredAttributes = attributes.filterNot {
      attr =>
        propMap.get(attr.name).isDefined
    }

    val declaredAttributes = attributes.diff(undeclaredAttributes)

    val violations: List[Violation] = (
        unmatchedMandatories.map(errorMandatory)
          ++
        undeclaredAttributes.map(errorUndeclared)
          ++
        typeViolations(declaredAttributes)
          ++
        constraintViolations(declaredAttributes)
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

  def constraintViolations(attributes: Seq[Attribute[Any]]): List[Violation] = {
    attributes.flatMap (
      attr => propMap(attr.name).constraints.flatMap(constraint => constraint.validate(attr))
    ).toList
  }

  // Violation builders
  def errorMandatory(decl: PropertyDecl) =
    Violation("mandatory", s"Field '${decl.name}' is mandatory.", Some(decl))
  def errorUndeclared(attr: Attribute[Any]) =
    Violation("undeclared", s"No declaration found for attribute '${attr.name}'", None, Some(attr))

}
