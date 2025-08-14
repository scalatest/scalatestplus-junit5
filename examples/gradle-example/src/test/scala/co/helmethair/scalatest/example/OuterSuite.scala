package co.helmethair.scalatest.example

import org.scalatest.Suite
import org.scalatest.funspec.AnyFunSpec

object OuterSuite {

  class Inner extends AnyFunSpec {
    it("a") {}
  }

  object I1 extends Inner
}
class OuterSuite extends AnyFunSpec {

  val i2 = new OuterSuite.Inner {
    override def suiteName: String = "i2"
  }

  override def nestedSuites: IndexedSeq[Suite] = {

    val i3 = new OuterSuite.Inner {
      override def suiteName: String = "i3"
    }

    IndexedSeq(
      OuterSuite.I1, // object
      i2, // member
      i3, // local instance
      new OuterSuite.Inner() {
        override def suiteName: String = "i4"
      } // ad-hoc
    )
  }

  it("b") {}
}
