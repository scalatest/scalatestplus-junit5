/*
 * Copyright 2001-2013 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatestplus.junit5

import org.junit.jupiter.api.ClassDescriptor
import org.junit.platform.commons.support.ReflectionSupport
import org.junit.platform.engine.TestDescriptor.Visitor
import org.junit.platform.engine.discovery.{ClassSelector, ClasspathRootSelector, FileSelector, PackageSelector, UniqueIdSelector}
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.support.discovery.EngineDiscoveryRequestResolver.InitializationContext
import org.junit.platform.engine.support.discovery.SelectorResolver.{Context, Match, Resolution}
import org.junit.platform.engine.support.discovery.{EngineDiscoveryRequestResolver, SelectorResolver}
import org.junit.platform.engine.{EngineDiscoveryRequest, ExecutionRequest, TestDescriptor, TestExecutionResult, UniqueId}
import org.scalatest.{Args, ConfigMap, DynaTags, Filter, Stopper, Tracker}

import java.util.Optional
import java.util.function.Supplier
import java.util.logging.Logger
import java.util.stream.Collectors
//import scala.jdk.CollectionConverters._
//import scala.jdk.OptionConverters._
import scala.collection.JavaConverters._
import scala.reflect.NameTransformer

/**
 * ScalaTest implementation for JUnit 5 Test Engine.
 */ 
class ScalaTestEngine extends org.junit.platform.engine.TestEngine {

  private val logger = Logger.getLogger(classOf[ScalaTestEngine].getName)

  /**
   * Test engine ID, return "scalatest".
   */
  def getId: String = "scalatest"

  /**
   * Discover ScalaTest suites, you can disable the discover by setting system property org.scalatestplus.junit5.ScalaTestEngine.disabled to "true".
   */
  def discover(discoveryRequest: EngineDiscoveryRequest, uniqueId: UniqueId): TestDescriptor = {
    // reference: https://blogs.oracle.com/javamagazine/post/junit-build-custom-test-engines-java
    //            https://software-matters.net/posts/custom-test-engine/

    val engineDesc = new EngineDescriptor(uniqueId, "ScalaTest EngineDescriptor")

    if (System.getProperty("org.scalatestplus.junit5.ScalaTestEngine.disabled") != "true") {
      logger.info("Starting test discovery...")

      val alwaysTruePredicate =
        new java.util.function.Predicate[String]() {
          def test(t: String): Boolean = true
        }

      val isSuitePredicate =
        new java.util.function.Predicate[Class[_]]() {
          def test(t: Class[_]): Boolean = classOf[org.scalatest.Suite].isAssignableFrom(t)
        }

      def classDescriptorFunction(aClass: Class[_]) =
        new java.util.function.Function[TestDescriptor, Optional[ScalaTestClassDescriptor]]() {
          def apply(parent: TestDescriptor): Optional[ScalaTestClassDescriptor] = {
            val suiteUniqueId = parent.getUniqueId.append(ScalaTestClassDescriptor.segmentType, aClass.getName)
            parent.getChildren.asScala.find(_.getUniqueId == suiteUniqueId) match {
              case Some(_) => Optional.empty[ScalaTestClassDescriptor]()
              case None => Optional.of(new ScalaTestClassDescriptor(engineDesc, suiteUniqueId, aClass))
            }
          }
        }

      val toMatch =
        new java.util.function.Function[TestDescriptor, java.util.stream.Stream[Match]]() {
          def apply(td: TestDescriptor): java.util.stream.Stream[Match] = {
            java.util.stream.Stream.of[Match](Match.exact(td))
          }
        }



      def addToParentFunction(context: SelectorResolver.Context) =
        new java.util.function.Function[Class[_], java.util.stream.Stream[Match]]() {
          def apply(aClass: Class[_]): java.util.stream.Stream[Match] = {
            context.addToParent(classDescriptorFunction(aClass))
              .map[java.util.stream.Stream[Match]](toMatch)
              .orElse(java.util.stream.Stream.empty())
          }
        }

      val classSelectorResolver = new SelectorResolver {

        override def resolve(selector: ClasspathRootSelector, context: SelectorResolver.Context): SelectorResolver.Resolution = {
          val matches =
            ReflectionSupport.findAllClassesInClasspathRoot(selector.getClasspathRoot, isSuitePredicate, alwaysTruePredicate)
              .stream()
              .flatMap(addToParentFunction(context))
              .collect(Collectors.toSet())
          Resolution.matches(matches)
        }

        override def resolve(selector: PackageSelector, context: SelectorResolver.Context): SelectorResolver.Resolution = {
          val matches =
            ReflectionSupport.findAllClassesInPackage(selector.getPackageName, isSuitePredicate, alwaysTruePredicate)
              .stream()
              .flatMap(addToParentFunction(context))
              .collect(Collectors.toSet())
          Resolution.matches(matches)
        }

        override def resolve(selector: ClassSelector, context: SelectorResolver.Context): SelectorResolver.Resolution = {
          val testClass = selector.getJavaClass
          if (isSuitePredicate.test(testClass)) {
            context.addToParent(
              new java.util.function.Function[TestDescriptor, Optional[ScalaTestClassDescriptor]]() {
                def apply(parent: TestDescriptor): Optional[ScalaTestClassDescriptor] = {
                  val suiteUniqueId = parent.getUniqueId.append(ScalaTestClassDescriptor.segmentType, testClass.getName)
                  parent.getChildren.asScala.find(_.getUniqueId == suiteUniqueId) match {
                    case Some(_) => Optional.empty[ScalaTestClassDescriptor]()
                    case None => Optional.of(new ScalaTestClassDescriptor(engineDesc, suiteUniqueId, testClass))
                  }
                }
              })
            .map[Resolution](
              new java.util.function.Function[TestDescriptor, Resolution]() {
                def apply(td: TestDescriptor): Resolution = Resolution.`match`(Match.exact(td))
              }
            ).orElse(Resolution.unresolved())
          }
          else
            Resolution.unresolved()
        }
      }

      val uniqueIdSelectorResolver = new SelectorResolver {
        override def resolve(selector: UniqueIdSelector, context: SelectorResolver.Context): SelectorResolver.Resolution = {
          selector.getUniqueId.getSegments.asScala.toList match {
            case engineSeg :: suiteSeg :: testSeg :: Nil if engineSeg.getType == "engine" && engineSeg.getValue == "scalatest" && testSeg.getType == "test" && suiteSeg.getType == ScalaTestClassDescriptor.segmentType =>
              val suiteClassName = suiteSeg.getValue
              val suiteClass = Class.forName(suiteClassName)
              if (classOf[org.scalatest.Suite].isAssignableFrom(suiteClass)) {
                context.addToParent(
                  new java.util.function.Function[TestDescriptor, Optional[ScalaTestClassDescriptor]]() {
                    def apply(parent: TestDescriptor): Optional[ScalaTestClassDescriptor] = {
                      val children = parent.getChildren.asScala
                      val suiteUniqueId = uniqueId.append(ScalaTestClassDescriptor.segmentType, suiteClass.getName)
                      val testUniqueId = suiteUniqueId.append("test", testSeg.getValue)
                      val testDesc = new ScalaTestDescriptor(testUniqueId, testSeg.getValue)
                      val (suiteDesc, result) =
                        children.find(_.getUniqueId == suiteUniqueId) match {
                          case Some(suiteDesc) =>
                            (suiteDesc, Optional.empty[ScalaTestClassDescriptor]())

                          case None =>
                            val suiteDesc = new ScalaTestClassDescriptor(engineDesc, suiteUniqueId, suiteClass)
                            (suiteDesc, Optional.of(suiteDesc))
                        }

                      suiteDesc.getChildren.asScala.find(_.getUniqueId == testUniqueId) match {
                        case Some(_) => // Do nothing if the test already exists
                        case None => suiteDesc.addChild(testDesc)
                      }

                      result
                    }
                  }
                )
                .map[Resolution](
                  new java.util.function.Function[TestDescriptor, Resolution]() {
                    def apply(td: TestDescriptor): Resolution = Resolution.`match`(Match.exact(td))
                  }
                )
                .orElse(Resolution.unresolved())
              }
              else
                Resolution.unresolved()

            case engineSeg :: suiteSeg :: Nil if engineSeg.getType == "engine" && engineSeg.getValue == "scalatest" && suiteSeg.getType == ScalaTestClassDescriptor.segmentType =>
              val suiteClassName = suiteSeg.getValue
              val suiteClass = Class.forName(suiteClassName)
              if (classOf[org.scalatest.Suite].isAssignableFrom(suiteClass)) {
                context.addToParent(
                  new java.util.function.Function[TestDescriptor, Optional[ScalaTestClassDescriptor]]() {
                    def apply(parent: TestDescriptor): Optional[ScalaTestClassDescriptor] = {
                      val children = parent.getChildren.asScala
                      val suiteUniqueId = uniqueId.append(ScalaTestClassDescriptor.segmentType, suiteClass.getName)
                      children.find(_.getUniqueId == suiteUniqueId) match {
                        case Some(_) => Optional.empty[ScalaTestClassDescriptor]()
                        case None => Optional.of(new ScalaTestClassDescriptor(engineDesc, suiteUniqueId, suiteClass))
                      }
                    }
                  }
                )
                .map[Resolution](
                  new java.util.function.Function[TestDescriptor, Resolution]() {
                    def apply(td: TestDescriptor): Resolution = Resolution.`match`(Match.exact(td))
                  }
                )
                .orElse(Resolution.unresolved())
              }
              else
                Resolution.unresolved()

            case _ => Resolution.unresolved()
          }
        }
      }

      val resolver = EngineDiscoveryRequestResolver.builder[EngineDescriptor]()
                     .addClassContainerSelectorResolver(isSuitePredicate)
                     .addSelectorResolver(classSelectorResolver)
                     .addSelectorResolver(uniqueIdSelectorResolver)
                     .build()

      resolver.resolve(discoveryRequest, engineDesc)

      logger.info("Completed test discovery, discovered suite count: " + engineDesc.getChildren.size())
    }

    engineDesc
  }

  /**
   * Execute ScalaTest suites, you can disable the ScalaTest suites execution by setting system property org.scalatestplus.junit.JUnit5TestEngine.disabled to "true".
   */
  def execute(request: ExecutionRequest): Unit = {
    if (System.getProperty("org.scalatestplus.junit5.ScalaTestEngine.disabled") != "true") {
      logger.info("Start tests execution...")
      val engineDesc = request.getRootTestDescriptor
      val listener = request.getEngineExecutionListener

      listener.executionStarted(engineDesc)
      engineDesc.getChildren.asScala.foreach { testDesc =>
        testDesc match {
          case clzDesc: ScalaTestClassDescriptor =>
            logger.info("Start execution of suite class " + clzDesc.suiteClass.getName + "...")
            listener.executionStarted(clzDesc)
            val suiteClass = clzDesc.suiteClass
            val canInstantiate = JUnitHelper.checkForPublicNoArgConstructor(suiteClass) && classOf[org.scalatest.Suite].isAssignableFrom(suiteClass)
            require(canInstantiate, "Must pass an org.scalatest.Suite with a public no-arg constructor")
            val suiteToRun = suiteClass.newInstance.asInstanceOf[org.scalatest.Suite]
            val reporter = new EngineExecutionListenerReporter(listener, clzDesc, engineDesc)
            val children = clzDesc.getChildren.asScala

            val filter =
              if (children.isEmpty)
                Filter()
              else {
                val SelectedTag = "Selected"
                val SelectedSet = Set(SelectedTag)
                val testNames = suiteToRun.testNames
                val desiredTests: Set[String] =
                  children.map(_.getDisplayName).filter { tn =>
                    testNames.contains(tn) || testNames.contains(NameTransformer.decode(tn))
                  }.toSet
                val taggedTests: Map[String, Set[String]] = desiredTests.map(_ -> SelectedSet).toMap
                val suiteId = suiteToRun.suiteId
                  Filter(
                    tagsToInclude = Some(SelectedSet),
                    excludeNestedSuites = true,
                    dynaTags = DynaTags(Map.empty, Map(suiteId -> taggedTests))
                  )
              }

            suiteToRun.run(None, Args(reporter,
              Stopper.default, filter, ConfigMap.empty, None,
              new Tracker, Set.empty))

            listener.executionFinished(clzDesc, TestExecutionResult.successful())

            logger.info("Completed execution of suite class " + clzDesc.suiteClass.getName + ".")

          case otherDesc =>
            // Do nothing for other descriptor, just log it.
            logger.warning("Found test descriptor " + otherDesc.toString + " that is not supported, skipping.")
        }
      }
      listener.executionFinished(engineDesc, TestExecutionResult.successful())
      logger.info("Completed tests execution.")
    }
  }
}
