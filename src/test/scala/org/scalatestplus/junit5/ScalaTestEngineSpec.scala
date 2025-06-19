package org.scalatestplus.junit5

import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.discovery.ClasspathRootSelector
import org.junit.platform.engine.discovery.DiscoverySelectors.{selectClasspathRoots, selectPackage}
import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder.request
import org.scalatest.{BeforeAndAfterAll, funspec}
import org.scalatestplus.junit5.helpers.HappySuite

import java.nio.file.{Files, Paths}
import scala.collection.JavaConverters._

class ScalaTestEngineSpec extends funspec.AnyFunSpec with BeforeAndAfterAll {
  val engine = new ScalaTestEngine
  var scalaTestEngineProperty: Option[String] = None

  override def beforeAll(): Unit = {
    scalaTestEngineProperty = Option(System.clearProperty("org.scalatestplus.junit5.ScalaTestEngine.disabled"))
  }

  override def afterAll(): Unit = {
    scalaTestEngineProperty.foreach(System.setProperty("org.scalatestplus.junit5.ScalaTestEngine.disabled", _))
  }

  describe("ScalaTestEngine") {
    describe("discover method") {
      it("should discover suites on classpath") {
        val classPathRoot = classOf[ScalaTestEngineSpec].getProtectionDomain.getCodeSource.getLocation
        val discoveryRequest = request.selectors(
          selectClasspathRoots(java.util.Collections.singleton(Paths.get(classPathRoot.toURI)))
        ).build()
        val engineDescriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()))
        assert(engineDescriptor.getChildren.asScala.exists(td => td.asInstanceOf[ScalaTestClassDescriptor].suiteClass == classOf[HappySuite]))
      }

      it("should return unresolved for classpath without any tests") {
        val emptyPath = Files.createTempDirectory(null)
        val discoveryRequest = request.selectors(
          selectClasspathRoots(java.util.Collections.singleton(emptyPath))
        ).build()

        val engineDescriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()))
        assert(engineDescriptor.getChildren.asScala.isEmpty)
      }

      it("should discover suites in package") {
        val discoveryRequest = request.selectors(
          selectPackage("org.scalatestplus.junit5.helpers")
        ).build()

        val engineDescriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()))
        assert(engineDescriptor.getChildren.asScala.exists(td => td.asInstanceOf[ScalaTestClassDescriptor].suiteClass == classOf[HappySuite]))
      }

      it("should return unresolved for package without any tests") {
        val discoveryRequest = request.selectors(
          selectPackage("org.scalatestplus.junit5.nonexistant")
        ).build()

        val engineDescriptor = engine.discover(discoveryRequest, UniqueId.forEngine(engine.getId()))
        assert(engineDescriptor.getChildren.asScala.isEmpty)
      }
    }
  }
}
