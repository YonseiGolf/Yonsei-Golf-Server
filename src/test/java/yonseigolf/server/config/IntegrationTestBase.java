package yonseigolf.server.config;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.DockerComposeContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.io.File;
import java.time.Duration;

@SpringBootTest
@Testcontainers
@ActiveProfiles("integration")
public abstract class IntegrationTestBase {

    private static final String MYSQL_SERVICE = "mysql";
    private static final int MYSQL_PORT = 3306;
    private static final String REDIS_SERVICE = "redis";
    private static final int REDIS_PORT = 6379;

    static DockerComposeContainer<?> dockerComposeContainer;

    static {
        dockerComposeContainer = new DockerComposeContainer<>(
            new File("src/test/resources/docker-compose.yml"))
            .withExposedService(MYSQL_SERVICE, MYSQL_PORT,
                Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(2)))
            .withExposedService(REDIS_SERVICE, REDIS_PORT,
                Wait.forListeningPort().withStartupTimeout(Duration.ofMinutes(1)));
        dockerComposeContainer.start();
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        String mysqlHost = dockerComposeContainer.getServiceHost(MYSQL_SERVICE, MYSQL_PORT);
        Integer mysqlPort = dockerComposeContainer.getServicePort(MYSQL_SERVICE, MYSQL_PORT);

        String redisHost = dockerComposeContainer.getServiceHost(REDIS_SERVICE, REDIS_PORT);
        Integer redisPort = dockerComposeContainer.getServicePort(REDIS_SERVICE, REDIS_PORT);

        registry.add("spring.datasource.url",
            () -> String.format("jdbc:mysql://%s:%d/yonsei_golf_test", mysqlHost, mysqlPort));
        registry.add("spring.datasource.username", () -> "root");
        registry.add("spring.datasource.password", () -> "test");

        registry.add("spring.redis.host", () -> redisHost);
        registry.add("spring.redis.port", () -> redisPort);
        registry.add("spring.redis.password", () -> "");
    }
}
