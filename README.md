# ScalaTest + JUnit 5
ScalaTest + JUnit provides integration support between ScalaTest and JUnit 5.

**Usage**

To use it for ScalaTest 3.2.19 and JUnit 5.10:

SBT:

```
libraryDependencies += "org.scalatestplus" %% "junit-5-10" % "3.2.19.0" % Test
```

Maven:

```
<dependency>
  <groupId>org.scalatestplus</groupId>
  <artifactId>junit-5-10_3</artifactId>
  <version>3.2.19.0</version>
  <scope>test</scope>
</dependency>
```

Gradle: 

```
dependencies {
    implementation "org.scala-lang:scala3-library:3.3.3"

    testImplementation "org.scalatest:scalatest_3:3.2.19"
    testImplementation "org.junit.platform:junit-platform-launcher:1.10.2"
    testRuntimeOnly "org.junit.platform:junit-platform-engine:1.10.2"
    testRuntimeOnly "org.scalatestplus:junit-5-10_3:3.2.19.0"
}

test {
    useJUnitPlatform {
        includeEngines 'scalatest'
        testLogging {
            events("passed", "skipped", "failed", "standard_error")
        }
    }
}
```

Gradle (Kotlin): 

```
dependencies {
    implementation("org.scala-lang:scala3-library:3.3.3")

    testImplementation("org.scalatest:scalatest_3:3.2.19")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.10.2")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.10.2")
    testRuntimeOnly("org.scalatestplus:junit-5-10_3:3.2.19.0")
}

tasks {
    test{
        useJUnitPlatform {
            includeEngines("scalatest")
            testLogging {
                events("passed", "skipped", "failed")
            }
        }
    }
}
```

**Main Features**

`scalatestplus-junit5` provides the following features:

  - ScalaTest JUnit test engine implementation, this allows ScalaTest suites to be run by JUnit 5 platform, include or exclude ScalaTest suites through tags is supported.
  - The `JUnitSuiteLike` and `JUnitSuite` trait that can be run by either ScalaTest runner or JUnit 5 platform.

**Note on Gradle Project's Default Test Runner on IntelliJ IDEA**

For Gradle project, by default IntelliJ IDEA uses Gradle's test runner to run tests, which at the time of writing does not work with `Jump to Source` feature.  You may switch to use IntelliJ IDEA's test runner by following the instructions [here](https://www.jetbrains.com/help/idea/work-with-tests-in-gradle.html#configure_gradle_test_runner).

**Publishing**

Please use the following commands to publish to Sonatype:

```
$ sbt +publishSigned
```