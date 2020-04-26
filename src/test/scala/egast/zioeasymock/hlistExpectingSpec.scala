package egast.zioeasymock

import zio.ZIO
import zio.test.Assertion._
import zio.test._

object hlistExpectingSpec extends DefaultRunnableSpec {
  override def spec = suite("zioeasymock")(
    testM("test mocking") {

      EasyMockInterpreter.testWithMock(
        MockWithExpectations(StandardMock[TestService.Service], HNil)
          .expect(_.doSomething(10000))(_.andReturn(ZIO.effectTotal("100")))
      )(mock =>
        assertM(mock.doSomething(1000))(equalTo("100"))
      )

    }
  )

}
