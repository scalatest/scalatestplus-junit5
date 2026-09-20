package org.scalatestplus.junit5

import org.junit.platform.engine.discovery.DiscoverySelectors.selectClass
import org.junit.platform.launcher.core.{LauncherDiscoveryRequestBuilder, LauncherFactory}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Suite, funspec}
import org.scalatestplus.junit5.integration.listener.{JUnitListener, ScalaTestListener}
import org.scalatestplus.junit5.integration.NestedFixture

import java.lang.reflect.Modifier
import java.nio.file.{Files, Path, Paths}
import java.util.jar.JarFile

class IntegrationTests extends funspec.AnyFunSpec with BeforeAndAfterAll with BeforeAndAfterEach {

  import scala.collection.JavaConverters._

  var scalaTestEngineProperty: Option[String] = None

  override def beforeAll(): Unit = {
    scalaTestEngineProperty = Option(System.clearProperty("org.scalatestplus.junit5.ScalaTestEngine.disabled"))
  }

  override def afterAll(): Unit = {
    scalaTestEngineProperty.foreach(System.setProperty("org.scalatestplus.junit5.ScalaTestEngine.disabled", _))
  }

  lazy val integrationDirPath: Path = {
    val classPathRoot = this.getClass.getProtectionDomain.getCodeSource.getLocation

    val root = Paths.get(classPathRoot.toURI)

    val result = root.resolve("org/scalatestplus/junit5/integration")

    println(s"integration path set to $result")
    result
  }

  private def findTestClasses(path: Path): Set[Class[_]] = {
    val suiteClass = classOf[Suite]

    def isTestClass(cls: Class[_]): Boolean = {
      suiteClass.isAssignableFrom(cls) && !Modifier.isAbstract(cls.getModifiers) && !cls.isInterface
    }

    val classPathRoot = Paths.get(getClass.getProtectionDomain.getCodeSource.getLocation.toURI)

    val classNames = if (Files.isDirectory(path)) {
      Files
        .list(path)
        .iterator()
        .asScala
        .filter(p => p.toString.endsWith(".class"))
        .map(p => classPathRoot.relativize(p).toString.replace('/', '.').dropRight(".class".length))
        .toSet
    } else if (path.toString.toLowerCase.endsWith(".jar")) {
      val jarFile = new JarFile(path.toFile)
      try {
        jarFile
          .entries()
          .asScala
          .filter(e => e.getName.endsWith(".class"))
          .map(_.getName.replace('/', '.').dropRight(".class".length))
          .toSet
      } finally {
        jarFile.close()
      }
    } else {
      Set.empty[String]
    }

    classNames
      .filterNot { name =>
        name.contains("$")
      }
      .flatMap { className =>
        try {
          val classLoader = getClass.getClassLoader
          val cls = Class.forName(className, false, classLoader)
          val result: Option[Class[_]] = Some(cls).filter(isTestClass)
          result
        } catch {
          case _: Throwable => None
        }
      }
  }

  lazy val integrationTests: Set[Class[_]] = findTestClasses(integrationDirPath);

  override def beforeEach(): Unit = {
    ScalaTestListener.clear()
    JUnitListener.clear()
  }

  describe("ScalaTest runner and JUnit runner should discovery & run identical tests") {

    describe("in classes") {

      integrationTests.foreach { clz =>
        val clzName = clz.getName
        it(s"${clz.getSimpleName}") {

          // with ScalaTest runner

          org.scalatest.tools.Runner.run(
            Array(
              "-R",
              integrationDirPath.toString,
              "-s",
              clzName,
              "-C",
              classOf[ScalaTestListener].getCanonicalName
              //            "-oN"
            )
          )

          // with JUnit 5 runner
          {
            val launcher = LauncherFactory.create()

            val discoveryRequest = LauncherDiscoveryRequestBuilder.request
              .selectors(
                selectClass(clzName)
              )
              .build()

            launcher.execute(discoveryRequest, new JUnitListener())
          }

          JUnitListener.startedShouldBe(ScalaTestListener.started.mkString("\n"))
          JUnitListener.finishedShouldBe(ScalaTestListener.finished.mkString("\n"))
        }
      }
    }

    ignore("in packages") { // TODO this doesn't work

      val pkg = classOf[NestedFixture].getPackage.getName
      org.scalatest.tools.Runner.run(
        Array(
          "-R",
          integrationDirPath.toString,
          "-w",
          pkg,
          "-C",
          classOf[ScalaTestListener].getCanonicalName
          //        "-oN"
        )
      )
    }
  }

}
