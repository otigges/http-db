package model.structure

import play.api.libs.json._
import play.api.libs.functional.syntax._

/**
 * Document (inspired by martinei)
 */
case class Document(links:  Map[String, DocLink] = Map() , embedded: Map[String, List[Document]] = Map(), attributes: JsObject = JsObject(Seq())) extends DocumentOrListOf {

   def withLink (kv : (String, DocLink)) = copy (links = links+(kv._1->kv._2))
   def withLinks (a :(String, DocLink), b : (String, DocLink), c : (String,DocLink)*) = copy(links = links+ (a,b, c: _*))

   def withEmbedded (kv : (String,DocumentOrListOf)) : Document = {
       val list = kv._2 match {
         case d: Document => List(d)
         case ListOfDocuments(l) => l
       }

     val newEmbedded = embedded + (kv._1 -> (embedded.getOrElse(kv._1, List()) ++ list))
     copy(embedded = newEmbedded)
   }

   def withAttributes[T : OWrites](attribs : T) = {
     val newAttribs = Json.toJson(attribs).as[JsObject]
     copy(attributes = attributes ++ newAttribs)
   }
 }

sealed trait DocumentOrListOf

case class ListOfDocuments(docs : List[Document]) extends DocumentOrListOf

object DocumentOrListOf {
  implicit def fromList(l : List[Document]) = ListOfDocuments(l)
}

object Document {
  implicit val writesDocument: Writes[Document] = {

    def writeEmbedded: OWrites[Map[String, List[Document]]] =
      OWrites[Map[String, List[Document]]]  { a=>
        a match {
          case m if m.isEmpty => Json.obj()
          case other =>
            (__ \ "_embedded").lazyWrite(play.api.libs.json.Writes.mapWrites[List[Document]]).writes(a)
        }
      }

    val r = ((__ \ "_links").write[Map[String,DocLink]] and
      writeEmbedded and
      (__ .write[JsObject]))
    r(unlift(Document.unapply))

  }
}

