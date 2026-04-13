plugins {
    id("application")
    id("io.ktor.plugin")
}

application {
    mainClass.set("api.ApplicationKt")
}

val ktorVersion = "2.3.12"
val koinVersion = "3.5.6"

dependencies {
    implementation(project(":domain"))
    implementation(project(":data"))
    implementation(project(":service"))
    
    // Ktor
    implementation("io.ktor:ktor-server-core-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-netty-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-auth-jwt-jvm:$ktorVersion")
    implementation("io.ktor:ktor-server-content-negotiation-jvm:$ktorVersion")
    implementation("io.ktor:ktor-serialization-kotlinx-json-jvm:$ktorVersion")
    
    // DI
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.insert-koin:koin-logger-slf4j:$koinVersion")
    
    // Swagger UI (smiley4 for DSL route docs)
    implementation("io.github.smiley4:ktor-swagger-ui:2.9.0")
    // Official Ktor Swagger for serving UI
    implementation("io.ktor:ktor-server-swagger-jvm:$ktorVersion")
    
    // Logging
    implementation("ch.qos.logback:logback-classic:1.5.6")
    
    // Redis
    implementation("io.lettuce:lettuce-core:6.3.2.RELEASE")
    
    // RabbitMQ
    implementation("com.rabbitmq:amqp-client:5.21.0")
    
    // Testing
    testImplementation("io.ktor:ktor-server-tests-jvm:$ktorVersion")
    testImplementation("org.testcontainers:junit-jupiter:1.19.7")
    testImplementation("org.testcontainers:postgresql:1.19.7")
    testImplementation("org.testcontainers:rabbitmq:1.19.7")
}
