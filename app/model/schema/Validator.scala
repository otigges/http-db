package model.schema

import model.Resource

trait Validator {

  def validate(resource: Resource) : ValidationResult

}

case class ValidationResult(violations: List[Violation]) {
  def isSuccess = violations.isEmpty
  def isError = !isSuccess
}

case class Violation(decl: PropertyDecl, id: String, msg: String)

class SchemaValidator(schema: ResourceSchema) extends Validator {

  override def validate(resource: Resource): ValidationResult = {
    val propMap: Map[String, PropertyDecl] = schema.properties.map(decl => (decl.name, decl)).toMap

    val unmatchedDecls = schema.properties.filter(
      decl => resource.attributeOption(decl.name).isEmpty && decl.cardinality.min > 0
    )

    val unmatchedAttributes = resource.attributes().filterNot {
      attr =>
        propMap.get(attr.name).isDefined
    }

    val violations: List[Violation] = unmatchedDecls.map(decl => Violation(decl, "mandatory",
        s"Field ${decl.name} is mandatory.")) ++
        unmatchedAttributes.map(attr => Violation(null, "undeclared", s"No declaration found for attribute ${attr.name}"))

    if (violations.isEmpty)
      success()
    else
      ValidationResult(violations)
  }

  def success() = ValidationResult(List())

}
