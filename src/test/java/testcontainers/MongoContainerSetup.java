package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.mongodb.MongoDb;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.Objects;

public abstract class MongoContainerSetup {

    private static final String STABLE_VERSION = "mongo:7.0.39";//minimum 4.4
    private static final String LATEST_VERSION = "mongo:8.3.7";

    private static final String DOCKER_IMAGE = resolveDockerImage();

    protected static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer(DOCKER_IMAGE)
            .withExposedPorts(27017)
            .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                    new PortBinding(Ports.Binding.bindPort(27017), new ExposedPort(27017))
            ))
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example_password");

    public static String expectedUrl;


    static {
        System.out.printf("""
                \u001B[32m
                ============================================
                >>> TESTS RUNNING ON ON DOCKER IMAGE: %s <<<
                ============================================
                \u001B[0m
                %n""", DOCKER_IMAGE);
        getCi();
        MONGO_CONTAINER.start();
    }

    private static String resolveDockerImage() {
        String dockerVersion = System.getProperty("dockerTestVersion");

        if ("latest".equalsIgnoreCase(dockerVersion)) {
            return LATEST_VERSION;
        }

        return STABLE_VERSION;
    }

    static void getCi() {
        if (Objects.equals(System.getenv("CI"), "true")) {
            expectedUrl = "mongodb://root:example_password@docker:27017";
        } else {
            expectedUrl = "mongodb://root:example_password@localhost:27017";
        }
    }

    public static MongoDb getMongo() {
        return new MongoDb(
                expectedUrl,
                "test_db");
    }

}
