plugins {
    java
    scala
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala-library:2.12.17")

    testImplementation("org.scalatest:scalatest_2.12:3.2.16")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.9.1")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.9.1")
    testRuntimeOnly("org.scalatestplus:junit-5-9_2.12:3.2.16.0-M5")
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
