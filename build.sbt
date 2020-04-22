inThisBuild(
  List(
    scalaVersion := "2.13.1",
    organization := "com.github.egast",
    licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")),
    developers := List(
      Developer("egast", "Erik Gast", "egast@users.noreply.github.com", url("https://github.com/egast"))
    ),
    scmInfo := Some(
      ScmInfo(url("https://github.com/egast/zio-easymock/"), "scm:git:git@github.com:egast/zio-easymock.git")
    )
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value

name := "zio-easymock"
version := "0.1.0-SNAPSHOT"

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
