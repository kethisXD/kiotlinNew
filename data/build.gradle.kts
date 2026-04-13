val exposedVersion = "0.50.0"

dependencies {
    implementation(project(":domain"))
    
    // DB & ORM
    implementation("org.jetbrains.exposed:exposed-core:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-dao:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.postgresql:postgresql:42.7.3")
    implementation("com.zaxxer:HikariCP:5.1.0")
    
    // Migrations
    implementation("org.flywaydb:flyway-core:10.12.0")
    implementation("org.flywaydb:flyway-database-postgresql:10.12.0")
    
    // Redis
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:1.8.0")
    
    // RabbitMQ
    implementation("com.rabbitmq:amqp-client:5.21.0")

    // Testing
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")
    testImplementation("org.testcontainers:postgresql:1.19.7")
}
