package org.scalatestplus.junit5.integration.listener

import org.junit.platform.engine.TestExecutionResult
import org.junit.platform.launcher.{TestExecutionListener, TestIdentifier}

object JUnitListener extends ExecutionOrder {}

class JUnitListener extends TestExecutionListener {
  import JUnitListener._

  override def executionStarted(testIdentifier: TestIdentifier): Unit = {
    if (testIdentifier.isTest) {

      started += List(testIdentifier.getDisplayName).mkString(" - ")
    }

  }

  override def executionFinished(
      testIdentifier: TestIdentifier,
      testExecutionResult: TestExecutionResult
  ): Unit = {

    if (testIdentifier.isTest) {

      finished.append(
        testExecutionResult.getStatus -> List(testIdentifier.getDisplayName).mkString(" - ")
      )
    }
  }
}
