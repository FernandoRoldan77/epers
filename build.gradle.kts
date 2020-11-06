import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    id("java")
    id("org.jetbrains.kotlin.plugin.jpa") version "1.3.72"
    id("org.springframework.boot") version "2.2.6.RELEASE"
    id("io.spring.dependency-management") version "1.0.9.RELEASE"
    id("org.jetbrains.kotlin.plugin.noarg") version "1.3.71"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.3.71"
    kotlin("jvm") version "1.3.71"
    kotlin("plugin.spring") version "1.3.71"

}

allOpen {
    annotation("javax.persistence.Entity")
    annotation("javax.persistence.MappedSuperclass")
    annotation("javax.persistence.Embeddable")
}

group = "ar.edu.unq.eperdemic"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
    mavenCentral()
}
dependencies {
    implementation("org.springframework.boot:spring-boot-starter-web")
    implementation("org.springframework.boot:spring-boot-starter-aop")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.hibernate:hibernate-core")
    implementation("mysql:mysql-connector-java")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
    implementation("org.mockito:mockito-core:3.3.3")
    testImplementation ("org.assertj:assertj-core:3.6.2")
    testImplementation("org.springframework.boot:spring-boot-starter-test") {
        exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
    }
    testImplementation("junit:junit:4.12")
    testImplementation ("org.mockito:mockito-inline:2.13.0")

    runtimeOnly("mysql:mysql-connector-java")

    implementation("org.apache.logging.log4j:log4j-core:2.13.1")

    implementation("org.neo4j.driver:neo4j-java-driver:4.0.1")

    implementation("org.mongodb:mongodb-driver-sync:4.0.1")
    implementation("org.mongodb:mongodb-driver-core:4.0.1")
    implementation("org.mongodb:bson:4.0.4")
}

tasks.withType<Test> {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
    kotlinOptions {
        freeCompilerArgs = listOf("-Xjsr305=strict")
        jvmTarget = "1.8"
    }
}
