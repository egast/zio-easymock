# zio-easymock
EasyMock mocking for ZIO

## Example usage
```scala
testM("mock two services") {
  expecting[TestService.Service, TestService2.Service] { (service1, service2) =>
    expectM(service1.doSomething(1000)).map(_.andReturn(ZIO.effectTotal("1000"))) *>
      expectM(service2.doSomething2(200)).map(_.andReturn(ZIO.effectTotal("200")))

  }.whenExecutingAsLayer(mockLayer =>
    assertM(TestService2.doSomething2(200) *> TestService.doSomething(1000))(equalTo("1000"))
      .provideCustomLayer(mockLayer)
  )
}
```
