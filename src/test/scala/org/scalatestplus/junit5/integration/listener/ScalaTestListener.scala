package org.scalatestplus.junit5.integration.listener

import org.junit.platform.engine.TestExecutionResult
import org.scalatest.Reporter
import org.scalatest.events._

object ScalaTestListener extends ExecutionOrder {

  case class IR(
      suiteClassName: Option[String],
      suiteName: String,
      testName: String
  ) {

    override def toString: String = {

      (List(suiteName, testName)).mkString(" - ")
    }
  }
}

// Custom reporter to capture test results
class ScalaTestListener extends Reporter {

  import ScalaTestListener._

  def apply(event: Event): Unit = {

    event match {
      case e: TestStarting =>
        val ir = IR(e.suiteClassName, e.suiteName, e.testName)
        started.append(ir.toString)

      case e: TestSucceeded =>
        val ir = IR(e.suiteClassName, e.suiteName, e.testName)
        finished.append(
          TestExecutionResult.Status.SUCCESSFUL -> ir.toString
        )

      case e: TestCanceled =>
        val ir = IR(e.suiteClassName, e.suiteName, e.testName)
        finished.append(
          TestExecutionResult.Status.ABORTED -> ir.toString
        )

      case e: TestFailed =>
        val ir = IR(e.suiteClassName, e.suiteName, e.testName)
        finished.append(TestExecutionResult.Status.FAILED -> ir.toString)

      case _ =>
    }
  }
}
