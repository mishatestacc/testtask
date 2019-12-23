val Http4sVersion = "0.21.0-M6"
val CirceVersion = "0.12.3"
val DoobieVersion = "0.8.8"
val PostgresVersion = "42.2.9"
val ScalaTestVersion = "3.1.0"

lazy val testtask = project
  .in(file("."))
  .settings(
    scalaVersion := "2.13.1",
    libraryDependencies ++= Seq(
      "ch.qos.logback" % "logback-classic" % "1.2.3",
      "org.http4s" %% "http4s-blaze-server" % Http4sVersion,
      "org.http4s" %% "http4s-blaze-client" % Http4sVersion,
      "org.http4s" %% "http4s-circe" % Http4sVersion,
      "org.http4s" %% "http4s-dsl" % Http4sVersion,
      "io.circe" %% "circe-generic" % CirceVersion,
      "org.tpolecat" %% "doobie-quill" % DoobieVersion,
      "org.postgresql" % "postgresql" % PostgresVersion,
      "org.scalatest" %% "scalatest" % ScalaTestVersion % Test,
      "org.scalamock" %% "scalamock" % "4.4.0" % Test
    ),
    scalacOptions ++= Seq(
      "-deprecation",
      "-encoding", "UTF-8",
      //"-language:higherKinds",
      //"-language:postfixOps",
      //"-feature",
      //"-Ypartial-unification",
      "-Xfatal-warnings",
    )
  )