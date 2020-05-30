lazy val scala211  = "2.11.12"
lazy val scala212  = "2.12.11"
lazy val scala213  = "2.13.2"
lazy val mainScala = scala213
lazy val allScala  = Seq(scala211, scala212, mainScala)

inThisBuild(
  List(
    scalaVersion := mainScala,
    crossScalaVersions := allScala,
    organization := "com.github.egast",
    homepage := Some(url("https://github.com/egast/zio-easymock")),
    licenses := List(
      "Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0")
    ),
    developers := List(
      Developer(
        "egast",
        "Erik Gast",
        "egast@users.noreply.github.com",
        url("https://github.com/egast")
      )
    ),
    scmInfo := Some(
      ScmInfo(
        url("https://github.com/egast/zio-easymock/"),
        "scm:git:git@github.com:egast/zio-easymock.git"
      )
    )
  )
)

ThisBuild / publishTo := sonatypePublishToBundle.value

name := "zio-easymock"
version := "0.3.0"

val zioVersion      = "1.0.0-RC20"
val easymockVersion = "4.2"

libraryDependencies ++= Seq(
  "dev.zio" %% "zio"          % zioVersion,
  "dev.zio" %% "zio-test"     % zioVersion,
  "dev.zio" %% "zio-test-sbt" % zioVersion % "test"
)
libraryDependencies ++= Seq("org.easymock" % "easymock" % easymockVersion)

testFrameworks := Seq(new TestFramework("zio.test.sbt.ZTestFramework"))
