import java.io.PrintWriter
import scala.io.Source

name := "junit-5.10"

organization := "org.scalatestplus"

version := "3.2.17.0"

homepage := Some(url("https://github.com/scalatest/scalatestplus-junit"))

licenses := List("Apache-2.0" -> url("http://www.apache.org/licenses/LICENSE-2.0"))

developers := List(
  Developer(
    "bvenners",
    "Bill Venners",
    "bill@artima.com",
    url("https://github.com/bvenners")
  ),
  Developer(
    "cheeseng",
    "Chua Chee Seng",
    "cheeseng@amaseng.com",
    url("https://github.com/cheeseng")
  )
)

scalaVersion := "2.13.11"

crossScalaVersions := List(
  "2.10.7",
  "2.11.12",
  "2.12.18",
  "2.13.11", 
  "3.1.3"
)

scalacOptions ++= Seq("-target:jvm-1.8")

/** Add src/main/scala-{2|3} to Compile / unmanagedSourceDirectories */
Compile / unmanagedSourceDirectories ++= {
  val sourceDir = (Compile / sourceDirectory).value
  CrossVersion.partialVersion(scalaVersion.value).map {
    case (0 | 3, _) => sourceDir / "scala-3"
    case (n, _) => sourceDir / s"scala-$n"
  }
}

val scalatestVersion = "3.2.17"
val junitVersion = "5.10.0"
val junitEngineVersion = "1.10.0"

libraryDependencies ++= Seq(
  "org.scalatest" %% "scalatest-core" % scalatestVersion,
  "org.junit.platform" % "junit-platform-launcher" % junitEngineVersion,
  "org.junit.platform" % "junit-platform-commons" % junitEngineVersion,
  "org.junit.jupiter" % "junit-jupiter-api" % junitVersion,
  "org.junit.jupiter" % "junit-jupiter-engine" % junitVersion,
  "org.scalatest" %% "scalatest-wordspec" % scalatestVersion % Test,
  "org.scalatest" %% "scalatest-funspec" % scalatestVersion % Test,
  "org.scalatest" %% "scalatest-funsuite" % scalatestVersion % Test,
  "org.scalatest" %% "scalatest-shouldmatchers" % scalatestVersion % Test
)

import scala.xml.{Node => XmlNode, NodeSeq => XmlNodeSeq, _}
import scala.xml.transform.{RewriteRule, RuleTransformer}

// skip dependency elements with a scope
pomPostProcess := { (node: XmlNode) =>
  new RuleTransformer(new RewriteRule {
    override def transform(node: XmlNode): XmlNodeSeq = node match {
      case e: Elem if e.label == "dependency"
          && e.child.exists(child => child.label == "scope") =>
        def txt(label: String): String = "\"" + e.child.filter(_.label == label).flatMap(_.text).mkString + "\""
        Comment(s""" scoped dependency ${txt("groupId")} % ${txt("artifactId")} % ${txt("version")} % ${txt("scope")} has been omitted """)
      case _ => node
    }
  }).transform(node).head
}

Test / testOptions :=
  Seq(
    Tests.Argument(TestFrameworks.ScalaTest,
      "-m", "org.scalatestplus.junit5",
    ))

Test / fork := true

Test / javaOptions  +="-Dorg.scalatestplus.junit5.ScalaTestEngine.disabled=true"

enablePlugins(SbtOsgi)

osgiSettings

OsgiKeys.exportPackage := Seq(
  "org.scalatestplus.junit5.*"
)

OsgiKeys.importPackage := Seq(
  "org.scalatest.*",
  "org.scalactic.*", 
  "scala.*;version=\"$<range;[==,=+);$<replace;"+scalaBinaryVersion.value+";-;.>>\"",
  "*;resolution:=optional"
)

OsgiKeys.additionalHeaders:= Map(
  "Bundle-Name" -> "ScalaTestPlusJUnit5",
  "Bundle-Description" -> "ScalaTest+JUnit5 is an open-source integration library between ScalaTest and JUnit 5 for Scala projects.",
  "Bundle-DocURL" -> "http://www.scalatest.org/",
  "Bundle-Vendor" -> "Artima, Inc."
)

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  Some("publish-releases" at nexus + "service/local/staging/deploy/maven2")
}

publishMavenStyle := true

Test / publishArtifact := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <scm>
    <url>https://github.com/scalatest/scalatestplus-junit5</url>
    <connection>scm:git:git@github.com:scalatest/scalatestplus-junit5.git</connection>
    <developerConnection>
      scm:git:git@github.com:scalatest/scalatestplus-junit5.git
    </developerConnection>
  </scm>
)

credentials += Credentials(Path.userHome / ".ivy2" / ".credentials")

// Temporary disable publishing of doc in dotty, can't get it to build.
Compile / packageDoc / publishArtifact := !scalaBinaryVersion.value.startsWith("3")

def docTask(docDir: File, resDir: File, projectName: String): File = {
  val docLibDir = docDir / "lib"
  val htmlSrcDir = resDir / "html"
  val cssFile = docLibDir / "template.css"
  val addlCssFile = htmlSrcDir / "addl.css"

  val css = Source.fromFile(cssFile).mkString
  val addlCss = Source.fromFile(addlCssFile).mkString

  if (!css.contains("pre.stHighlighted")) {
    val writer = new PrintWriter(cssFile)

    try {
      writer.println(css)
      writer.println(addlCss)
    }
    finally { writer.close }
  }

  if (projectName.contains("scalatest")) {
    (htmlSrcDir * "*.gif").get.foreach { gif =>
      IO.copyFile(gif, docLibDir / gif.name)
    }
  }
  docDir
}

Compile / doc  := docTask((Compile / doc).value,
                          (Compile / sourceDirectory).value,
                          name.value)

Compile / doc / scalacOptions := Seq("-doc-title", s"ScalaTest + JUnit5 ${version.value}", 
                                       "-sourcepath", baseDirectory.value.getAbsolutePath(), 
                                       "-doc-source-url", s"https://github.com/scalatest/releases-source/blob/main/scalatestplus-junit5/${version.value}€{FILE_PATH}.scala")