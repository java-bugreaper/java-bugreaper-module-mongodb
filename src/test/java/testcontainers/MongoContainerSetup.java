package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.modules.mongodb.MongoDb;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.Objects;

public abstract class MongoContainerSetup {


    protected static final MongoDBContainer MONGO_CONTAINER = new MongoDBContainer("mongo:8.2.7")
            .withExposedPorts(27017)
            .withCreateContainerCmdModifier(cmd -> Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                    new PortBinding(Ports.Binding.bindPort(27017), new ExposedPort(27017))
            ))
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example_password");

    public static String expectedUrl;

    static {
        MONGO_CONTAINER.start();
        getCi();
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
