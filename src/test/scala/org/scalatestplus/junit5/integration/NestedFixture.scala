package org.scalatestplus.junit5.integration

import org.scalatest.Suite
import org.scalatest.funspec.AnyFunSpec

import scala.collection.immutable

object NestedFixture {

  class Inner extends AnyFunSpec {
    it("a") {}
  }

  object I1 extends Inner
}
class NestedFixture extends AnyFunSpec {

  val i2 = new NestedFixture.Inner {
    override def suiteName: String = "i2"
  }

  override def nestedSuites: immutable.IndexedSeq[Suite] = {

    val i3 = new NestedFixture.Inner {
      override def suiteName: String = "i3"
    }

    immutable.IndexedSeq[Suite](
      NestedFixture.I1, // object
      i2, // member
      i3, // local instance
      new NestedFixture.Inner() {
        override def suiteName: String = "i4"
      } // ad-hoc
    )
  }

  it("b") {}
}
