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

import org.junit.platform.engine.reporting.ReportEntry
import org.junit.platform.engine.support.descriptor.EngineDescriptor
import org.junit.platform.engine.{EngineExecutionListener, TestDescriptor, TestExecutionResult, UniqueId}
import org.opentest4j.AssertionFailedError
import org.scalactic.source
import org.scalatest.funspec
import org.scalatest.events.{Ordinal, TestFailed}
import org.scalatest.exceptions.{StackDepthException, TestFailedException}
import org.scalatestplus.junit5.helpers.HappySuite

class EngineExecutionListenerReporterSpec extends funspec.AnyFunSpec {

  private class CapturingListener extends EngineExecutionListener {
    var result: Option[TestExecutionResult] = None
    override def dynamicTestRegistered(testDescriptor: TestDescriptor): Unit = ()
    override def executionSkipped(testDescriptor: TestDescriptor, reason: String): Unit = ()
    override def executionStarted(testDescriptor: TestDescriptor): Unit = ()
    override def executionFinished(testDescriptor: TestDescriptor, testExecutionResult: TestExecutionResult): Unit =
      result = Some(testExecutionResult)
    override def reportingEntryPublished(testDescriptor: TestDescriptor, entry: ReportEntry): Unit = ()
  }

  private def testFailedException(message: String, cause: Option[Throwable]): TestFailedException =
    new TestFailedException((_: StackDepthException) => Some(message), cause, source.Position.here)

  // Fires one TestFailed event at the reporter and hands back the throwable it reported to JUnit.
  private def reportedThrowable(message: String, throwable: Option[Throwable]): Throwable = {
    val engineDesc = new EngineDescriptor(UniqueId.forEngine("scalatest"), "ScalaTest EngineDescriptor")
    val suiteUniqueId = engineDesc.getUniqueId.append(ScalaTestClassDescriptor.segmentType, classOf[HappySuite].getName)
    val clzDesc = new ScalaTestClassDescriptor(engineDesc, suiteUniqueId, classOf[HappySuite], true)
    val listener = new CapturingListener
    val reporter = new EngineExecutionListenerReporter(listener, clzDesc, engineDesc)

    reporter(
      TestFailed(
        new Ordinal(99),
        message,
        "HappySuite",
        classOf[HappySuite].getName,
        Some(classOf[HappySuite].getName),
        "a test",
        "a test",
        Vector.empty,
        Vector.empty,
        throwable
      )
    )

    val result = listener.result.getOrElse(fail("no execution result was reported"))
    assert(result.getStatus == TestExecutionResult.Status.FAILED)
    if (result.getThrowable.isPresent) result.getThrowable.get else null
  }

  describe("EngineExecutionListenerReporter") {

    it("should report a TestFailedException as an opentest4j AssertionFailedError carrying the event message") {
      val reported = reportedThrowable("the decorated message, with all the detail", Some(testFailedException("0 did not equal 2", None)))

      assert(reported.isInstanceOf[JUnitTestFailedError])
      assert(reported.isInstanceOf[AssertionFailedError])
      assert(reported.getMessage == "the decorated message, with all the detail")
    }

    it("should keep the stack trace of the original TestFailedException") {
      val tfe = testFailedException("boom", None)

      val reported = reportedThrowable("decorated", Some(tfe))

      assert(reported.getStackTrace.toList == tfe.getStackTrace.toList)
    }

    it("should carry over the cause and any suppressed throwables") {
      val cause = new IllegalArgumentException("the cause")
      val suppressed = new AssertionError("the suppressed one")
      val tfe = testFailedException("boom", Some(cause))
      tfe.addSuppressed(suppressed)

      val reported = reportedThrowable("decorated", Some(tfe))

      assert(reported.getCause eq cause)
      assert(reported.getSuppressed.toList == List(suppressed))
    }

    it("should pass a throwable that is not a TestFailedException through untouched") {
      val boom = new RuntimeException("not an assertion failure")

      assert(reportedThrowable("decorated", Some(boom)) eq boom)
    }

    it("should report null when the event carries no throwable") {
      assert(reportedThrowable("decorated", None) == null)
    }
  }
}
