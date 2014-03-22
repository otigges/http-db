package storage.base

import model.{Resource, QualifiedName}

trait ResourceResolver {

  def resolve(qn: QualifiedName) : Resource

}
