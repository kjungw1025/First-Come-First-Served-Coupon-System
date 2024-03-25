val bootJar: org.springframework.boot.gradle.tasks.bundling.BootJar by tasks

bootJar.enabled = false

repositories {
    mavenCentral()
}

dependencies {
    // spring boot
    implementation("org.springframework.boot:spring-boot-starter")

    // redisson
    implementation("org.redisson:redisson-spring-boot-starter:3.16.4")

    // jackson
    implementation("com.fasterxml.jackson.datatype:jackson-datatype-jsr310")
    implementation("com.fasterxml.jackson.core:jackson-databind")

    // memory cache
    implementation("com.github.ben-manes.caffeine:caffeine")

    // test
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
