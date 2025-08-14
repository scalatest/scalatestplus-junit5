package org.scalatestplus.junit5.nested

import org.scalatest.Suite
import org.scalatest.funspec.AnyFunSpec

object OuterSuite {

  class Inner extends AnyFunSpec {
    it("a") {}
  }

  object I1 extends Inner
  object I2 extends Inner
}
class OuterSuite extends AnyFunSpec {

  override def nestedSuites: IndexedSeq[Suite] = {
    println("hit!")

    IndexedSeq(
      OuterSuite.I1,
      OuterSuite.I2
    )
  }

  it("b") {}
}
