# zio-easymock
EasyMock mocking for ZIO

## Quickstart
Add the following dependency to your `build.sbt` file:
```
libraryDependencies ++= Seq(
  "com.github.egast" %% "zio-easymock" % "0.1.0"
)
```

### Example usage
```scala
import egast.zioeasymock._
import zio._
import zio.test.Assertion._
import zio.test._

object ExampleSpec extends DefaultRunnableSpec {
  override def spec = suite("example")(
    testM("mock one service as layer") {
      expecting[TestService.Service] { service1 =>
        expect(service1.multiplyByTwo(1000)).map(_.andReturn(ZIO.effectTotal(2000)))
      }.whenExecutingAsLayer(mockLayer =>
        assertM(TestService.multiplyByTwo(1000))(equalTo(2000))
          .provideCustomLayer(mockLayer)
      )
    },
    testM("mock two services as layer") {
      expecting[TestService.Service, TestService2.Service] { (service1, service2) =>
        expect(service1.multiplyByTwo(1000)).map(_.andReturn(ZIO.effectTotal(2000))) *>
          expect(service2.intToString(200)).map(_.andReturn(ZIO.effectTotal("200")))
      }.whenExecutingAsLayer(mockLayer =>
        assertM(TestService2.intToString(200) *> TestService.multiplyByTwo(1000))(equalTo(2000))
          .provideCustomLayer(mockLayer)
      )
    },
    testM("mock one service") {
      expecting[TestService.Service] { service1 =>
        expect(service1.multiplyByTwo(1000)).map(_.andReturn(ZIO.effectTotal(2000)))
      }.whenExecuting(service =>
        assertM(service.multiplyByTwo(1000))(equalTo(2000))
      )
    },
    testM("mock service with strict mocks") {
      createStrictMock[TestService.Service]
        .expecting { service1 =>
          expect(service1.multiplyByTwo(200)).map(_.andReturn(ZIO.effectTotal(400))) *>
            expect(service1.multiplyByTwo(400)).map(_.andReturn(ZIO.effectTotal(800)))
        }.whenExecutingAsLayer(mockLayer =>
        assertM(TestService.multiplyByTwo(200) >>= TestService.multiplyByTwo)(equalTo(800))
          .provideCustomLayer(mockLayer)
      )
    }
  )
}

object TestService {
  type TestService = Has[Service]

  trait Service {
    def multiplyByTwo(n: Int): ZIO[Any, Throwable, Int]
  }

  def multiplyByTwo(n: Int): ZIO[TestService, Throwable, Int] =
    ZIO.accessM(_.get[Service].multiplyByTwo(n))
}

object TestService2 {
  type TestService2 = Has[Service]

  trait Service {
    def intToString(n: Int): ZIO[Any, Throwable, String]
  }

  def intToString(n: Int): ZIO[TestService2, Throwable, String] =
    ZIO.accessM(_.get[Service].intToString(n))
}
```
