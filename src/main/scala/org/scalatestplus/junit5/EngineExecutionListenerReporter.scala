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

import org.junit.platform.engine.{EngineExecutionListener, TestDescriptor, TestExecutionResult, UniqueId}
import org.scalatest.{Resources => _, _}
import org.scalatest.events._

private[junit5] class EngineExecutionListenerReporter(listener: EngineExecutionListener, clzDesc: ScalaTestClassDescriptor, engineDesc: TestDescriptor) extends Reporter {

  // This form isn't clearly specified in JUnit docs, but some tools may assume it, so why rock the boat.
  // Here's what JUnit code does:
  //   public static Description createTestDescription(Class<?> clazz, String name, Annotation... annotations) {
  //       return new Description(String.format("%s(%s)", name, clazz.getName()), annotations);
  //   }
  // So you can see the test name shows up, which is normally a test method name, followed by the fully qualified class name in parens
  // We put test name and suite class name (or suite name if no class) in parens, but don't try and do anything to get rid of spaces or
  // parens the test or suite names themselves, since it is unclear if this format is used by anyone anyway. If actual bug reports come
  // in, then we can fix each actual problem once it is understood.
  //
  private def testDescriptionName(suiteName: String, suiteClassName: Option[String], testName: String) =
    suiteClassName match {
      case Some(suiteClassName) => testName + "(" + suiteClassName + ")"
      case None => testName + "(" + suiteName + ")"
    }

  private def suiteDescriptionName(suiteName: String, suiteClassName: Option[String]) =
    suiteClassName match {
      case Some(suiteClassName) => suiteClassName
      case None => suiteName
    }

  private def createTestDescriptor(suiteId: String, suiteName: String, suiteClassName: Option[String], testName: String, locationOpt: Option[Location]): ScalaTestDescriptor = {
    val uniqueId = clzDesc.theUniqueId.append("test", testName)
    new ScalaTestDescriptor(uniqueId, testName, locationOpt)
  }

  override def apply(event: Event): Unit = {

    event match {

      case TestStarting(ordinal, suiteName, suiteId, suiteClassName, testName, testText, formatter, location, rerunnable, payload, threadName, timeStamp) =>
        val testDesc = createTestDescriptor(suiteId, suiteName, suiteClassName, testName, location)
        clzDesc.addChild(testDesc)
        listener.dynamicTestRegistered(testDesc)
        listener.executionStarted(testDesc)

      case TestFailed(ordinal, message, suiteName, suiteId, suiteClassName, testName, testText, recordedEvents, analysis, throwable, duration, formatter, location, rerunnable, payload, threadName, timeStamp) =>
        val throwableOrNull = throwable.orNull
        val testDesc = createTestDescriptor(suiteId, suiteName, suiteClassName, testName, location)
        listener.executionFinished(testDesc, TestExecutionResult.failed(throwableOrNull))

      case TestSucceeded(ordinal, suiteName, suiteId, suiteClassName, testName, testText, recordedEvents, duration, formatter, location, rerunnable, payload, threadName, timeStamp) =>
        val testDesc = createTestDescriptor(suiteId, suiteName, suiteClassName, testName, location)
        listener.executionFinished(testDesc, TestExecutionResult.successful())

      case TestIgnored(ordinal, suiteName, suiteId, suiteClassName, testName, testText, formatter, location, payload, threadName, timeStamp) =>
        val testDesc = createTestDescriptor(suiteId, suiteName, suiteClassName, testName, location)
        listener.executionSkipped(testDesc, "Test ignored.")

      // TODO: I dont see TestCanceled here. Probably need to add it
      // Closest thing we can do with pending is report an ignored test
      case TestPending(ordinal, suiteName, suiteId, suiteClassName, testName, testText, recordedEvents, duration, formatter, location, payload, threadName, timeStamp) =>
        val testDesc = createTestDescriptor(suiteId, suiteName, suiteClassName, testName, location)
        listener.executionSkipped(testDesc, "Test pending.")

      case SuiteAborted(ordinal, message, suiteName, suiteId, suiteClassName, throwable, duration, formatter, location, rerunnable, payload, threadName, timeStamp) =>
        val throwableOrNull = throwable.orNull
        listener.executionFinished(clzDesc, TestExecutionResult.aborted(throwableOrNull))

      case RunAborted(ordinal, message, throwable, duration, summary, formatter, location, payload, threadName, timeStamp) =>
        val throwableOrNull = throwable.orNull
        listener.executionFinished(engineDesc, TestExecutionResult.aborted(throwableOrNull))

      case _ =>
    }
  }

  // In the unlikely event that a message is blank, use the throwable's detail message
  def messageOrThrowablesDetailMessage(message: String, throwable: Option[Throwable]): String = {
    val trimmedMessage = message.trim
    if (!trimmedMessage.isEmpty)
      trimmedMessage
    else
      throwable.map(_.getMessage.trim).getOrElse("")
  }
}
