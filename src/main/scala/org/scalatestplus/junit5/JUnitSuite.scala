/*
 * Copyright 2001-2023 Artima, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.scalatestplus.junit5;

/**
 * A suite of tests that can be run with either JUnit or ScalaTest. This class allows you to write JUnit 5 tests
 * with ScalaTest's more concise assertion syntax as well as JUnit's assertions (<code>assertEquals</code>, etc.).
 * You create tests by defining methods that are annotated with <code>Test</code>, and can create fixtures with
 * methods annotated with <code>Before</code> and <code>After</code>. For example:
 *
 * <pre class="stHighlight">
 * import org.junit.jupiter.api.{BeforeEach, Test}
 * import org.scalatestplus.junit5.JUnitSuite
 * import scala.collection.mutable.ListBuffer
 *
 * class TwoSuite extends JUnitSuite {
 *
 *   var sb: StringBuilder = _
 *   var lb: ListBuffer[String] = _
 *
 *   @BeforeEach def initialize() {
 *     sb = new StringBuilder("ScalaTest is ")
 *     lb = new ListBuffer[String]
 *   }
 *
 *   @Test def verifyEasy() {
 *     sb.append("easy!")
 *     assert(sb.toString === "ScalaTest is easy!")
 *     assert(lb.isEmpty)
 *     lb += "sweet"
 *   }
 *
 *   @Test def verifyFun() {
 *     sb.append("fun!")
 *     assert(sb.toString === "ScalaTest is fun!")
 *     assert(lb.isEmpty)
 *   }
 *
 * }
 * </pre>
 *
 * <p>
 * This version of <code>JUnitSuite</code> was tested with JUnit version 5.13.
 * </p>
 *
 * <p>
 * Instances of this class are not thread safe.
 * </p>
 *
 * @author Bill Venners
 * @author Daniel Watson
 * @author Joel Neely
 * @author Chua Chee Seng
 */
class JUnitSuite extends JUnitSuiteLike
