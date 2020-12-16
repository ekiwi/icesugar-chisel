name := "icesugar-chisel"
version := "0.1"
scalaVersion := "2.12.12"

scalacOptions := Seq("-deprecation", "-unchecked", "-Xsource:2.11")

libraryDependencies += "edu.berkeley.cs" %% "chisel3" % "3.4.1"
libraryDependencies += "edu.berkeley.cs" %% "chiseltest" % "0.3.1" % Test
libraryDependencies += "org.scalatest" %% "scalatest" % "3.2.2" % Test

scalaSource in Compile := baseDirectory.value / "src"
scalaSource in Test := baseDirectory.value / "test"
resourceDirectory in Test := baseDirectory.value / "test" / "resources"
