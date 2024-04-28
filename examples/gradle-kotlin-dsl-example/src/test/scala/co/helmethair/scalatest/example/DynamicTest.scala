package co.helmethair.scalatest.example

import org.scalatest.funsuite.AnyFunSuiteLike

class DynamicTest extends AnyFunSuiteLike {

  test(s"(won't run with gradle or intellij single run) this is dynamic value -> ${System.currentTimeMillis()}") {
  }

  test(s"(wwill run with gradle, intellij class run and intellij single run) this is test with static name") {
  }

}
