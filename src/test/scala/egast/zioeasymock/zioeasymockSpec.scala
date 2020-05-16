package egast.zioeasymock

import org.easymock.EasyMock
import zio.test.Assertion._
import zio.test.TestAspect._
import zio.test._
import zio.{Has, Task, ZIO}

object zioeasymockSpec extends DefaultRunnableSpec {
  override def spec = suite("zioeasymock")(
    testM("test mocking") {
      expecting[TestService.Service, TestService2.Service] { (service1, service2) =>
        expect(service1.doSomething(1000))(_.andReturn(ZIO.effectTotal("100"))) *>
          expect(service2.doSomething2(200))(_.andReturn(ZIO.effectTotal("200")))

      }.whenExecutingAsLayer(mockLayer =>
        assertM(TestService2.doSomething2(200) *> TestService.doSomething(1000))(equalTo("100"))
          .provideCustomLayer(mockLayer)
      )
    },
    testM("test strict mocking") {
      createStrictMock[TestService.Service]
        .expecting { service1 =>
          expect(service1.doSomething(1000))(_.andReturn(ZIO.effectTotal("1000"))) *>
            expect(service1.doSomething(2000))(_.andReturn(ZIO.effectTotal("2000"))) *>
            expect(service1.doSomething(3000))(_.andReturn(ZIO.effectTotal("3000")))
        }.whenExecutingAsLayer(mockLayer =>
        assertM(TestService.doSomething(1000) *> TestService.doSomething(2000) *> TestService.doSomething(3000))(equalTo("3000"))
          .provideCustomLayer(mockLayer)
      )
    },
    testM("test strict mocking unexpected call") {
      createStrictMock[TestService.Service]
        .expecting { service1 =>
          expect(service1.doSomething(2000))(_.andReturn(ZIO.effectTotal("2000"))) *>
            expect(service1.doSomething(1000))(_.andReturn(ZIO.effectTotal("1000"))) *>
            expect(service1.doSomething(3000))(_.andReturn(ZIO.effectTotal("3000")))

        }.whenExecutingAsLayer(mockLayer =>
        assertM(TestService.doSomething(1000) *> TestService.doSomething(2000) *> TestService.doSomething(3000))(equalTo("3000"))
          .provideCustomLayer(mockLayer)
      )
    } @@ failing,
    testM("test mocking with mock returning a failed effect") {
      val failure = new RuntimeException("failure")
      expecting[TestService.Service, TestService2.Service] { (_, service2) =>
        expect(service2.doSomething2(200))(_.andReturn(ZIO.fail(failure)))

      }.whenExecutingAsLayer(mockLayer =>
        assertM((TestService2.doSomething2(200) *> TestService.doSomething(1000)).run)(fails(equalTo(failure)))
          .provideCustomLayer(mockLayer)
      )
    },
    testM("test mocking with capture") {
      for {
        capture <- Task.effect(EasyMock.newCapture[Int]())
        result <- expecting[TestService.Service, TestService2.Service] { (service1, service2) =>
          expect(service1.doSomething(EasyMock.capture(capture)))(_.andReturn(ZIO.effect(capture.getValue.toString))) *>
            expect(service2.doSomething2(200))(_.andReturn(ZIO.effectTotal("200")))

        }.whenExecuting((service1, service2) =>
          (
            for {
              _ <- service2.doSomething2(200)
              result <- service1.doSomething(1000)
            } yield assert(result)(equalTo("1000")) &&
              assert(capture.getValue)(equalTo(1000))
            )
        )
      } yield result
    },
    testM("test mocking unexpected call failure") {
      expecting[TestService.Service, TestService2.Service] { (service1, service2) =>
        expect(service1.doSomething(100))(_.andReturn(ZIO.effectTotal("100"))) *>
          expect(service2.doSomething2(200))(_.andReturn(ZIO.effectTotal("200")))

      }.whenExecutingAsLayer(mockLayer =>
        assertM(TestService2.doSomething2(200) *> TestService.doSomething(1000))(equalTo("100"))
          .provideCustomLayer(mockLayer)
      )
    } @@ failing,
    testM("test mocking create mock failure") {
      expecting[String] { string =>
        expect(string.charAt(100))(_.andReturn('e'))

      }.whenExecuting(string =>
        assertM(ZIO.effect(string.charAt(100)))(equalTo('e'))
      )
    } @@ failing,
    testM("test mocking verify failure") {
      expecting[TestService.Service, TestService2.Service] { (service1, service2) =>
        expect(service1.doSomething(1000))(_.andReturn(ZIO.effectTotal("100"))) *>
          expect(service1.doSomething(1000))(_.andReturn(ZIO.effectTotal("100"))) *>
          expect(service2.doSomething2(200))(_.andReturn(ZIO.effectTotal("200")))

      }.whenExecutingAsLayer(mockLayer =>
        assertM(TestService2.doSomething2(200) *> TestService.doSomething(1000))(equalTo("100"))
          .provideCustomLayer(mockLayer)
      )
    } @@ failing,
    testM("test mocking with check") {
      checkM(Gen.int(0, 200)) { n =>
        expecting[TestService.Service, TestService2.Service] { (service1, service2) =>
          expect(service1.doSomething(n))(_.andReturn(ZIO.effectTotal((n).toString))) *>
            expect(service2.doSomething2(n * 2))(_.andReturn(ZIO.effectTotal((n * 2).toString)))

        }.whenExecutingAsLayer(mockLayer =>
          assertM(TestService2.doSomething2(n * 2) *> TestService.doSomething(n))(equalTo(n.toString))
            .provideCustomLayer(mockLayer)
        )
      }
    },
    testM("test mocking with check should fail") {
      checkM(Gen.int(0, 200)) { n =>
        expecting[TestService.Service, TestService2.Service] { (service1, service2) =>
          expect(service1.doSomething(n))(_.andReturn(ZIO.effectTotal((n % 100).toString))) *>
            expect(service2.doSomething2(n * 2))(_.andReturn(ZIO.effectTotal((n * 2).toString)))

        }.whenExecutingAsLayer(mockLayer =>
          assertM(TestService2.doSomething2(n * 2) *> TestService.doSomething(n))(equalTo(n.toString))
            .provideCustomLayer(mockLayer)
        )
      }
    } @@ failing,
    testM("test nested mocking ") {
      expecting[TestService.Service] { service1 =>
        expect(service1.doSomething(1000))(_.andReturn(ZIO.effectTotal("100"))) *>
          expect(service1.doSomething(2000))(_.andReturn(ZIO.effectTotal("2000")))
      }.whenExecutingAsLayer(mockLayer =>
        expecting[TestService2.Service] { service2 =>
          expect(service2.doSomething2(200))(_.andReturn(ZIO.effectTotal("200")))
        }.whenExecutingAsLayer(mock2Layer =>
          assertM(TestService2.doSomething2(200) *> TestService.doSomething(1000) *> TestService.doSomething(2000))(equalTo("2000"))
            .provideCustomLayer(mockLayer ++ mock2Layer)
        )
      )
    },
    testM("test mocking") {
      checkM(Gen.listOf(Gen.anyInt)) { numbers =>
        expecting[TestService.Service] { service1 =>
          ZIO.foreach(numbers)(n =>
            expect(service1.doSomething(n))(_.andReturn(ZIO.effectTotal(n.toString))))
        }.whenExecutingAsLayer(mockLayer =>
          assertM(
            ZIO.foreach(numbers)(n => TestService.doSomething(n))
          )(equalTo(numbers.map(_.toString)))
            .provideCustomLayer(mockLayer)
        )
      }
    }
  )

}

private object TestService {
  type TestService = Has[Service]

  trait Service {
    def doSomething(key: Int): ZIO[Any, Throwable, String]
  }

  def doSomething(key: Int): ZIO[TestService, Throwable, String] =
    ZIO.accessM(_.get[Service].doSomething(key))
}


private object TestService2 {
  type TestService2 = Has[Service]

  trait Service {
    def doSomething2(key: Int): ZIO[Any, Throwable, String]
  }

  def doSomething2(key: Int): ZIO[TestService2, Throwable, String] =
    ZIO.accessM(_.get[Service].doSomething2(key))
}