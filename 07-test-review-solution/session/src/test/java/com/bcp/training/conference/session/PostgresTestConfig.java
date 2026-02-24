package com.bcp.training.conference.session;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class PostgresTestConfig {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("postgres:14.1"))
            .withDatabaseName("sessions")
            .withUsername("postgres")
            .withPassword("postgres");

    static {
        postgres.start();
    }
}
