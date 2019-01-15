resolvers ++= Seq(
  Resolver.sbtPluginRepo("releases"),
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  "Sonatype repository" at "https://oss.sonatype.org/content/repositories/snapshots"
)

addSbtPlugin("org.tpolecat" % "tut-plugin" % "0.6.10")
addSbtPlugin("io.get-coursier" % "sbt-coursier" % "1.1.0-SNAPSHOT")
