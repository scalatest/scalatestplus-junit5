/*
 * Copyright 2001-2023 Artima, Inc.
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

import org.junit.platform.engine.support.descriptor.{AbstractTestDescriptor, ClassSource}
import org.junit.platform.engine.{TestDescriptor, TestSource, TestTag, UniqueId}
import org.scalatest.{Suite, TagAnnotation}

import scala.collection.JavaConverters._
import java.util.Optional

/**
 * <code>TestDescriptor</code> for ScalaTest suite.
 *
 * @param parent The parent descriptor.
 * @param theUniqueId The unique ID.
 * @param suiteClass The class of the ScalaTest suite.
 */
class ScalaTestClassDescriptor(parent: TestDescriptor, val theUniqueId: UniqueId, val suiteClass: Class[_], autoAddTestChildren: Boolean) extends AbstractTestDescriptor(theUniqueId, suiteClass.getName, ClassSource.from(suiteClass)) {

  /**
   * Suite instance that will be executed if this class descriptor is selected.
   */
  lazy val suite: Suite = {
    val canInstantiate = JUnitHelper.checkForPublicNoArgConstructor(suiteClass) && classOf[org.scalatest.Suite].isAssignableFrom(suiteClass)
    require(canInstantiate, "Must pass an org.scalatest.Suite with a public no-arg constructor")
    suiteClass.newInstance.asInstanceOf[org.scalatest.Suite]
  }

  // If this descriptor is created from container selectors (class, package, module etc.), children will be added automatically.
  if (autoAddTestChildren)
    suite.testNames.foreach { tn =>
      val testUniqueId = theUniqueId.append("test", tn)
      val testDesc = new ScalaTestDescriptor(testUniqueId, tn, None)
      addChild(testDesc)
    }

  /**
   * Type of this <code>ScalaTestClassDescriptor</code>.
   *
   * @return <code>TestDescriptor.Type.CONTAINER</code>
   */
  override def getType: TestDescriptor.Type = TestDescriptor.Type.CONTAINER

  /**
   * Override <code>mayRegisterTests</code> to return true
   *
   * @return <code>true</code>
   */
  override def mayRegisterTests(): Boolean = true

  /**
   * Return <code>ClassSource</code> for the given suite class.
   *
   * @return <code>ClassSource</code> created from given suite class
   */
  override def getSource: Optional[TestSource] =
    Optional.of(ClassSource.from(suiteClass))

  /**
   * Get tags for this suite.
   *
   * @return Tags for this suite.
   */
  override def getTags: java.util.Set[TestTag] =
    suiteClass.getAnnotations.filter((a) => a.annotationType.isAnnotationPresent(classOf[TagAnnotation])).map((a) => TestTag.create(a.annotationType.getName)).toSet.asJava
}

/**
 * <code>ScalaTestClassDescriptor</code> companion object.
 */
object ScalaTestClassDescriptor {
  /**
   * Segment type for <code>ScalaTestClassDescriptor</code>, has the value of <code>class</code>
   */
  val segmentType = "class"
}
