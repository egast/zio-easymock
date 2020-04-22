name := "zio-easymock"

version := "0.1"

scalaVersion := "2.13.1"

val zioVersion = "1.0.0-RC18-2"
val easymockVersion = "4.2"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio" % zioVersion,
  "dev.zio" %% "zio-test" % zioVersion,
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
)
libraryDependencies ++= Seq(
  "org.easymock" % "easymock" % easymockVersion
)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))