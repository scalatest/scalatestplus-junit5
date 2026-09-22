package co.helmethair.scalatest.example

import org.scalatest.Suite
import org.scalatest.funspec.AnyFunSpec

class NestedTestGroup extends AnyFunSpec {

  import NestedTestGroup.*

  override def nestedSuites: IndexedSeq[Suite] = IndexedSeq(
    N1,
    N2
  )

  it("c") {}
}

object NestedTestGroup {

  class NestedSubTest extends AnyFunSpec {

    it("a") {}
    it("b") {}
  }

  object N1 extends NestedSubTest
  object N2 extends NestedSubTest

}