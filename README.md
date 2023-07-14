# ScalaTest + JUnit 5
ScalaTest + JUnit provides integration support between ScalaTest and JUnit 5.

**Usage**

To use it for ScalaTest 3.2.16 and JUnit 5.9:

SBT:

```
libraryDependencies += "org.scalatestplus" %% "junit-5-9" % "3.2.16.0-M7" % Test
```

Maven:

```
<dependency>
  <groupId>org.scalatestplus</groupId>
  <artifactId>junit-5-9_2.13</artifactId>
  <version>3.2.16.0-M7</version>
  <scope>test</scope>
</dependency>
```

Gradle: 

```
dependencies {
    implementation "org.scala-lang:scala-library:2.13.11"

    testImplementation "org.scalatest:scalatest_2.13:3.2.16"
    testImplementation "org.junit.platform:junit-platform-launcher:1.9.1"
    testRuntimeOnly "org.junit.platform:junit-platform-engine:1.9.1"
    testRuntimeOnly "org.scalatestplus:junit-5-9_2.13:3.2.16.0-M7"
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
    implementation("org.scala-lang:scala-library:2.13.11")

    testImplementation("org.scalatest:scalatest_2.13:3.2.16")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.9.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.1")
    testRuntimeOnly("org.scalatestplus:junit-5-9_2.13:3.2.16.0-M7")
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