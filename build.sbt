name := "icesugar-chisel"
version := "0.1"
scalaVersion := "2.13.5"

scalacOptions := Seq("-deprecation", "-unchecked", "-feature", "-language:reflectiveCalls")

// using the rollings SNAPSHOT release for the latest features
resolvers += Resolver.sonatypeRepo("snapshots")
libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.5-SNAPSHOT"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.5-SNAPSHOT" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test
addCompilerPlugin("edu.berkeley.cs" % "chisel3-plugin" % "3.5-SNAPSHOT" cross CrossVersion.full)

scalaSource in Compile := baseDirectory.value / "src"
scalaSource in Test := baseDirectory.value / "test"
resourceDirectory in Test := baseDirectory.value / "test" / "resources"
