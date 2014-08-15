package model

import com.github.nscala_time.time.Imports._

/**
 * Context of an association
 */
trait Context {

  def from() : Option[DateTime] = None
  def until() : Option[DateTime] = None

}

object Unspecified extends Context
