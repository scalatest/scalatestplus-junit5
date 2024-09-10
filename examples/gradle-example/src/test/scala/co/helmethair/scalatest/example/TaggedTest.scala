package co.helmethair.scalatest.example

import org.scalatest.Tag
import org.scalatest.flatspec.AnyFlatSpec

object TagOne extends Tag("TagOne")

object TagTwo extends Tag("TagTwo")

class TaggedTest extends AnyFlatSpec {

  "Excluded tests" should "should not run" taggedAs TagOne in {
    fail("This test should not run")
  }

  "Included tests" should "run" taggedAs TagTwo in {}
}

class ExcludedTestsSuite extends AnyFlatSpec {

  "A test suite with all tests excluded" should "should not run" taggedAs TagOne in {
    fail("This test should not run")
  }
}
