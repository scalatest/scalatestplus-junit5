plugins {
    java
    scala
}

repositories {
    jcenter()
    mavenCentral()
}

dependencies {
    implementation("org.scala-lang:scala3-library_3:3.3.4")

    testImplementation("org.scalatest:scalatest_3:3.2.19")
    testRuntimeOnly("org.junit.platform:junit-platform-engine:1.11.3")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.11.3")
    testRuntimeOnly("org.scalatestplus:junit-5-11_3:3.2.19.0")
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
