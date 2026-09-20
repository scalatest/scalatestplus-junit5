package org.scalatestplus.junit5.integration

import org.scalatest.funspec.AnyFunSpec
import org.scalatestplus.junit5.integration.listener

import scala.sys.error

class PlainFixture_Fail extends AnyFunSpec {

  it("a") {}

  it("b") {
    error("!")
  }
}
