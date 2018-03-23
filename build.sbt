inThisBuild {
  Seq(
    scalaOrganization := "org.typelevel",
    scalaVersion := "2.12.4-bin-typelevel-4"
  )
}

scalacOptions ++= Seq(
  "-language:higherKinds",
  "-Yliteral-types"
)

enablePlugins(TutPlugin)

tutTargetDirectory := baseDirectory.value

resolvers += Resolver.bintrayRepo("ovotech", "maven")

libraryDependencies ++= Seq(
  "is.cir" %% "ciris-cats",
  "is.cir" %% "ciris-cats-effect",
  "is.cir" %% "ciris-core",
  "is.cir" %% "ciris-enumeratum",
  "is.cir" %% "ciris-refined"
).map(_ % "0.9.2")

libraryDependencies ++= Seq(
  "org.http4s" %% "http4s-dsl",
  "org.http4s" %% "http4s-blaze-server"
).map(_ % "0.18.5")

libraryDependencies ++= Seq(
  "com.ovoenergy" %% "ciris-kubernetes" % "0.5",
  "org.typelevel" %% "kittens" % "1.0.0-RC3",
  "eu.timepit" %% "refined-cats" % "0.8.7"
)

libraryDependencies += "com.lihaoyi" %% "utest" % "0.6.4" % Test

testFrameworks += new TestFramework("utest.runner.Framework")
