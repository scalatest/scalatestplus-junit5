# ScalaTest + JUnit 5
ScalaTest + JUnit provides integration support between ScalaTest and JUnit 5.

**Usage**

To use it for ScalaTest 3.2.16 and JUnit 5.9:

SBT:

```
libraryDependencies += "org.scalatestplus" %% "junit-5-9" % "3.2.16.0-M3" % Test
```

Maven:

```
<dependency>
  <groupId>org.scalatestplus</groupId>
  <artifactId>junit-5-9_2.13</artifactId>
  <version>3.2.16.0-M3</version>
  <scope>test</scope>
</dependency>
```

**Note on Gradle Project's Default Test Runner on IntelliJ IDEA**

For Gradle project, by default IntelliJ IDEA uses Gradle's test runner to run tests, which at the time of writing does not work with `Jump to Source` feature.  You may switch to use IntelliJ IDEA's test runner by following the instructions [here](https://www.jetbrains.com/help/idea/work-with-tests-in-gradle.html#configure_gradle_test_runner).

**Publishing**

Please use the following commands to publish to Sonatype:

```
$ sbt +publishSigned
```