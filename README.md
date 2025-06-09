# ScalaTest + JUnit 5
ScalaTest + JUnit provides integration support between ScalaTest and JUnit 5.

Official Website: https://www.scalatest.org/plus/junit5

💖 Support ScalaTest
--------------------

[![Sponsor ScalaTest](https://img.shields.io/badge/sponsor-scalatest-ff69b4?logo=github-sponsors)](https://github.com/sponsors/scalatest)

ScalaTest has been a cornerstone of testing in the Scala ecosystem for over 17 years. It’s trusted by countless developers and teams to write expressive, flexible, and robust tests. We’ve always believed in keeping ScalaTest free and open source, but maintaining a tool used so widely takes time, care, and ongoing effort.

If ScalaTest has saved you time, helped you ship better software, or become a key part of your development workflow, please consider supporting our work. Your sponsorship helps us dedicate time to fixing bugs, improving documentation, adding new features, and keeping ScalaTest reliable for the entire community.

👉 [Become a sponsor for ScalaTest](https://github.com/sponsors/scalatest) to help keep Scala’s most widely used testing library thriving!

**Usage**

To use it for ScalaTest 3.2.19 and JUnit 5.13:

SBT:

```
libraryDependencies += "org.scalatestplus" %% "junit-5-13" % "3.2.19.0" % Test
```

Maven:

```
<dependency>
  <groupId>org.scalatestplus</groupId>
  <artifactId>junit-5-13_3</artifactId>
  <version>3.2.19.0</version>
  <scope>test</scope>
</dependency>
```

Gradle:

```
dependencies {
    implementation "org.scala-lang:scala3-library:3.3.5"

    testImplementation "org.scalatest:scalatest_3:3.2.19"
    testImplementation "org.junit.platform:junit-platform-launcher:1.13.1"
    testRuntimeOnly "org.junit.platform:junit-platform-engine:1.13.1"
    testRuntimeOnly "org.scalatestplus:junit-5-13_3:3.2.19.0"
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
    implementation("org.scala-lang:scala3-library:3.3.5")

    testImplementation("org.scalatest:scalatest_3:3.2.19")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.13.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.1")
    testRuntimeOnly("org.scalatestplus:junit-5-13_3:3.2.19.0")
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
