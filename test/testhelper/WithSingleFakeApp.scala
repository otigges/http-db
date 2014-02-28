package testhelper

import org.specs2.mutable.Specification
import org.specs2.specification.Fragments
import org.specs2.specification.Step
import play.api.Play
import play.api.test.FakeApplication


trait WithSingleFakeApp extends Specification {
  
   lazy val app : FakeApplication = {
        FakeApplication()
    }

  // see http://bit.ly/11I9kFM (specs2 User Guide)
  override def map(fragments: =>Fragments) = {
    beforeAll()
    fragments ^ Step(afterAll)
  }


  def beforeAll() =  Play.start(app)
  def afterAll() =  Play.stop()

}