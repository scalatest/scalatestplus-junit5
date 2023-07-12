/*
 * Copyright 2001-2022 Artima, Inc.
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

  lazy val suite: Suite = {
    val canInstantiate = JUnitHelper.checkForPublicNoArgConstructor(suiteClass) && classOf[org.scalatest.Suite].isAssignableFrom(suiteClass)
    require(canInstantiate, "Must pass an org.scalatest.Suite with a public no-arg constructor")
    suiteClass.newInstance.asInstanceOf[org.scalatest.Suite]
  }

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

  override def mayRegisterTests(): Boolean = true

  override def getSource: Optional[TestSource] =
    Optional.of(ClassSource.from(suiteClass))

  override def getTags: java.util.Set[TestTag] =
    suiteClass.getAnnotations.filter((a) => a.annotationType.isAnnotationPresent(classOf[TagAnnotation])).map((a) => TestTag.create(a.annotationType.getName)).toSet.asJava
}

object ScalaTestClassDescriptor {
  val segmentType = "class"
}
