package com.bcp.training.conference.session;

import org.springframework.boot.test.context.TestConfiguration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration
public class PostgresTestConfig {

    // START: invalid image causes test failure - change to postgres:14.1 in solution
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>(
            DockerImageName.parse("invalid/postgresql/image"))
            .withDatabaseName("sessions")
            .withUsername("postgres")
            .withPassword("postgres");

    static {
        postgres.start();
    }
}
