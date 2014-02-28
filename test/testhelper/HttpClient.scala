package testhelper

import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.api.libs.json._
import scala.concurrent.Future

trait HttpClient {
  
  def post (call : Call, json :String): Any = {
    post (call.url, json)
  }
  
  def post (url : String, json : String): Future[SimpleResult] = {
    val fakeRequest = FakeRequest(Helpers.POST, url).withJsonBody(Json.parse(json))
    route(fakeRequest).get
  }
  
  
  def put (call : Call, json :String): Any = {
    put (call.url, json)
  }
  
  def put (url : String, json : String): Future[SimpleResult] = {
    put (url, Json.parse(json))
  }

  def put (call : Call, json :JsValue): Future[SimpleResult] = {
    put (call.url, json)
  }
  
  def put (url : String, json :JsValue): Future[SimpleResult] = {
    val fakeRequest = FakeRequest(Helpers.PUT, url).withJsonBody(json)
    route(fakeRequest).get
  }

  def get(call : Call) : Future[SimpleResult] = get (call.url)
  
  def get (url : String): Future[SimpleResult] = {
       val fakeRequest = FakeRequest(Helpers.GET, url)
      route(fakeRequest).get    
  }
  
}