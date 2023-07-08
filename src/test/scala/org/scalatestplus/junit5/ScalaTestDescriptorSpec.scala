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

import org.scalatest._
import org.scalatest.events._
import org.junit.platform.engine.UniqueId
import org.junit.platform.engine.support.descriptor.{ClassSource, FilePosition, FileSource, MethodSource}

import java.io.File

class ScalaTestDescriptorSpec extends funspec.AnyFunSpec {

  val uniqueId = UniqueId.forEngine("ScalaTestEngine")
  val displayName = "Test"
  val className = "org.example.TestClass"
  val methodName = "testMethod"
  val methodArgs = "arg1, arg2"
  val lineNumber = 42
  val fileName = "TestFile.scala"
  val filePathname = Some("/path/to/TestFile.scala")

  describe("ScalaTestDescriptor") {

    describe("getSource method") {

      it("should return ClassSource when location is TopOfClass") {
        val locationOpt = Some(TopOfClass(className))
        val descriptor1 = new ScalaTestDescriptor(uniqueId.append(ScalaTestClassDescriptor.segmentType, className).append("test", displayName), displayName, locationOpt)
        assert(descriptor1.getSource.isPresent)
        assert(descriptor1.getSource.get() == ClassSource.from(className))
      }

      it("should return MethodSource when location is TopOfMethod") {
        val descriptor1 = new ScalaTestDescriptor(uniqueId.append(ScalaTestClassDescriptor.segmentType, className).append("test", displayName), displayName,
                                                  Some(TopOfMethod(className, "public long java.util.concurrent.CountDownLatch.getCount()")))
        assert(descriptor1.getSource.isPresent)
        assert(descriptor1.getSource.get() == MethodSource.from(className, "getCount"))

        val descriptor2 = new ScalaTestDescriptor(uniqueId.append(ScalaTestClassDescriptor.segmentType, className).append("test", displayName), displayName,
          Some(TopOfMethod(className, "public final void java.lang.Object.wait(long,int) throws java.lang.InterruptedException")))
        assert(descriptor2.getSource.isPresent)
        assert(descriptor2.getSource.get() == MethodSource.from(className, "wait", "long,int"))

        val descriptor3 = new ScalaTestDescriptor(uniqueId.append(ScalaTestClassDescriptor.segmentType, className).append("test", displayName), displayName,
          Some(TopOfMethod(className, "public boolean java.lang.Object.equals(java.lang.Object)")))
        assert(descriptor3.getSource.isPresent)
        assert(descriptor3.getSource.get() == MethodSource.from(className, "equals", "java.lang.Object"))
      }

      it("should return FileSource when location is LineInFile") {
        val locationOpt = Some(LineInFile(lineNumber, fileName, filePathname))
        val descriptor1 = new ScalaTestDescriptor(uniqueId.append(ScalaTestClassDescriptor.segmentType, className).append("test", displayName), displayName, locationOpt)
        assert(descriptor1.getSource.isPresent)
        assert(descriptor1.getSource.get() == FileSource.from(new File(filePathname.getOrElse(fileName)), FilePosition.from(lineNumber)))
      }

      it("should return ClassSource when location is SeeStackDepthException") {
        val locationOpt = Some(SeeStackDepthException)
        val descriptor1 = new ScalaTestDescriptor(uniqueId.append(ScalaTestClassDescriptor.segmentType, className).append("test", displayName), displayName, locationOpt)
        assert(descriptor1.getSource.isPresent)
        assert(descriptor1.getSource.get() == ClassSource.from(className))
      }

      it("should return Optional.empty when location is None") {
        val locationOpt = None
        val descriptor1 = new ScalaTestDescriptor(uniqueId.append(ScalaTestClassDescriptor.segmentType, className).append("test", displayName), displayName, locationOpt)
        assert(!descriptor1.getSource.isPresent)
      }

    }

  }
}
