plugins {
    id("java")
    id("org.sonarqube") version "7.3.0.8198"
    id("com.gradleup.shadow") version "9.0.0-beta13"
    jacoco
    checkstyle
    application
}

group = "hexlet.code"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    implementation("io.javalin:javalin:6.1.3")
    implementation("gg.jte:jte:3.2.4")
    implementation("io.javalin:javalin-rendering:6.1.3")
    implementation("org.slf4j:slf4j-simple:2.0.7")
    implementation("org.postgresql:postgresql:42.7.7")
    implementation("com.h2database:h2:2.2.220")
    implementation("com.zaxxer:HikariCP:5.0.1")
    implementation("org.projectlombok:lombok:1.18.46")
    implementation("com.konghq:unirest-java:3.14.5")
    implementation("org.jsoup:jsoup:1.17.2")
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("io.javalin:javalin-testtools:6.1.3")
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("com.squareup.okhttp3:mockwebserver3:5.3.0")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

application {
    mainClass = "hexlet.code.App"
}

sonar {
    properties {
        property("sonar.projectKey", "snevg04_java-project-72")
        property("sonar.organization", "sn-evg-04")
        property(
            "sonar.coverage.jacoco.xmlReportPaths",
            "${layout.buildDirectory.get()}/reports/jacoco/test/jacocoTestReport.xml"
        )
    }
}

jacoco {
    toolVersion = "0.8.11"
}

tasks.named<Test>("test") {
    useJUnitPlatform()
}

tasks.named<JacocoReport>("jacocoTestReport") {
    dependsOn(tasks.test)
    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }
}

tasks.test {
    finalizedBy(tasks.jacocoTestReport)
}