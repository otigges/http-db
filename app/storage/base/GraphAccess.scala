package storage.base

import model.{QualifiedName, Resource}

trait GraphAccess {

  def findResource(qn : QualifiedName) : Option[Resource]

  def store(res : Resource) : Resource

  def delete(qn : QualifiedName)

}
