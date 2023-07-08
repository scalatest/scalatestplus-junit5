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

import org.junit.platform.engine.support.descriptor.{AbstractTestDescriptor, ClassSource, FilePosition, FileSource, MethodSource}
import org.junit.platform.engine.{TestDescriptor, TestSource, UniqueId}
import org.scalatest.events._

import java.io.File
import java.util.Optional

/**
 * <code>TestDescriptor</code> for a test in ScalaTest suite.
 *
 * @param theUniqueId The unique ID.
 * @param suiteClass The display name for this test.
 */
class ScalaTestDescriptor(theUniqueId: UniqueId, displayName: String, locationOpt: Option[Location]) extends AbstractTestDescriptor(theUniqueId, displayName) {
  /**
   * Type of this <code>ScalaTestDescriptor</code>.
   *
   * @return <code>TestDescriptor.Type.TEST</code>
   */
  override def getType: TestDescriptor.Type = TestDescriptor.Type.TEST

  override def getSource: Optional[TestSource] = {
    Optional.ofNullable(
      locationOpt.map { loc =>
        loc match {
          case TopOfClass(className) => ClassSource.from(className)
          case TopOfMethod(className, methodId) =>
            val openingBracketIdx = methodId.indexOf("(")
            val closingBracketIdx = methodId.indexOf(")")
            if (openingBracketIdx >= 0 && closingBracketIdx >= 0) {
              val beforeOpeningBracket = methodId.substring(0, openingBracketIdx)
              val lastSpaceIdx = beforeOpeningBracket.lastIndexOf(" ")
              val methodNameWithClassName = if (lastSpaceIdx >= 0) beforeOpeningBracket.substring(lastSpaceIdx + 1) else beforeOpeningBracket
              val methodNameLastDotIdx = methodNameWithClassName.lastIndexOf(".")
              val methodName = if (methodNameLastDotIdx >= 0) methodNameWithClassName.substring(methodNameLastDotIdx + 1) else methodNameWithClassName
              val methodArgs = methodId.substring(openingBracketIdx + 1, closingBracketIdx)
              if (methodArgs.trim.isEmpty) MethodSource.from(className, methodName) else MethodSource.from(className, methodName, methodArgs)
            }
            else
              MethodSource.from(className, methodId)

          case LineInFile(lineNumber: Int, fileName: String, filePathname: Option[String]) => FileSource.from(new File(filePathname.getOrElse(fileName)), FilePosition.from(lineNumber))
          case SeeStackDepthException => ClassSource.from(theUniqueId.getSegments.get(1).getValue) // Let's just refer to the class for SeeStackDepthException
        }
      }.getOrElse(null)
    )
  }
}
