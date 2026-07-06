package com.sportbuddy;

import com.sportbuddy.service.DataInitializer;
import com.sportbuddy.service.GigaChatGrpcService;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.testcontainers.containers.PostgreSQLContainer;

/**
 * Base class for integration tests. Boots the full Spring context against a
 * real PostgreSQL provided by Testcontainers (wired in via {@link ServiceConnection}).
 * <p>
 * The container is a JVM-wide singleton (started once in a static initializer and
 * reused across all integration test classes, letting Spring cache the context).
 * <p>
 * External collaborators that would reach the network are replaced with mocks:
 * the GigaChat gRPC client and the {@link DataInitializer} seeding runner.
 */
@SpringBootTest
@ActiveProfiles("test")
public abstract class AbstractIntegrationTest {

    @ServiceConnection
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine");

    static {
        POSTGRES.start();
    }

    /** Never dial the real GigaChat gRPC service from tests. */
    @MockitoBean
    protected GigaChatGrpcService gigaChatGrpcService;

    /** Skip data seeding so each test starts from a clean, deterministic schema. */
    @MockitoBean
    protected DataInitializer dataInitializer;
}
