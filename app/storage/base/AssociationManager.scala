package storage.base

import model.{Association, Link, Resource}

/**
 *
 */
class AssociationManager {

  def addLinks(subject: Resource, links: Link*)(implicit resolver : ResourceResolver) : Resource = {
    val associations: Seq[Association] = links.map {
      link => new Association(subject, resolver.resolve(link.predicate), resolver.resolve(link.target), link.context)
    }

    subject ++ (associations: _*)
  }

}
