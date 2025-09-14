plugins {
    id("java")
}

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform(libs.spring.boot.bom))

    testImplementation(libs.spring.boot.starter.test)
    testImplementation(libs.spring.security.test)

    testImplementation(libs.spring.boot.starter.web)

    testImplementation(libs.spring.boot.starter.data.jpa)
    testImplementation(libs.spring.boot.starter.validation)

    testCompileOnly(libs.swagger.annotations)

    testImplementation(libs.spring.boot.testcontainers)
    testImplementation(libs.testcontainers.junit)
    testImplementation(libs.testcontainers.postgresql)

    testImplementation(project(":app"))

    testCompileOnly(libs.lombok)
    testAnnotationProcessor(libs.lombok)


    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.test {
    useJUnitPlatform()
    maxParallelForks = 1
    forkEvery = 1

}