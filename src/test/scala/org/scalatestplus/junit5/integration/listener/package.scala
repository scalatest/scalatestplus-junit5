package org.scalatestplus.junit5.integration

import org.junit.jupiter.api.Assertions
import org.junit.platform.engine.TestExecutionResult

import scala.collection.mutable.ListBuffer

package object listener {

  type QN = String

  trait ExecutionOrder {

    val started = new ListBuffer[QN]
    val finished = new ListBuffer[(TestExecutionResult.Status, QN)]

    def clear(): Unit = {
      started.clear()
      finished.clear()
    }

    // TODO: need to make OS-agnostic
    def startedShouldBe(v: String = null): Unit = {
      assert(started.nonEmpty, "no test started")
      if (v == null)
        throw new IllegalArgumentException("expecting ground truth for started:\n\n" + started.mkString("\n"))
      Assertions.assertEquals(
        "\n" + v.trim + "\n",
        "\n" + started.mkString("\n").trim + "\n"
      )
    }

    def finishedShouldBe(v: String = null): Unit = {
      assert(started.nonEmpty, "no test finished")
      if (v == null)
        throw new IllegalArgumentException("expecting ground truth for finished:\n\n" + finished.mkString("\n"))
      Assertions.assertEquals(
        "\n" + v.trim + "\n",
        "\n" + finished.mkString("\n").trim + "\n"
      )
    }
  }

}
