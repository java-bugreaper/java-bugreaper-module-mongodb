package testcontainers;

import com.github.dockerjava.api.model.ExposedPort;
import com.github.dockerjava.api.model.PortBinding;
import com.github.dockerjava.api.model.Ports;
import net.bugreaper.core.config.YamlUtils;
import net.bugreaper.modules.mongodb.MongoDb;
import org.junit.jupiter.api.BeforeEach;
import org.testcontainers.mongodb.MongoDBContainer;

import java.util.Objects;

public class MongoSetup {


    MongoDBContainer container = new MongoDBContainer("mongo:8.2.7")
            .withExposedPorts(27017)
            .withCreateContainerCmdModifier(cmd -> {
                Objects.requireNonNull(cmd.getHostConfig()).withPortBindings(
                        new PortBinding(Ports.Binding.bindPort(27017), new ExposedPort(27017))
                );
            })
            .withEnv("MONGO_INITDB_ROOT_USERNAME", "root")
            .withEnv("MONGO_INITDB_ROOT_PASSWORD", "example_password");


    private static MongoSetup instance;
    private static final String CI = System.getenv("CI");
    public static String expectedUrl;


    @BeforeEach
    void getCi(){
        if(Objects.equals(CI, "true")){
            expectedUrl = "mongodb://root:example_password@docker:27017";
        }else {
            expectedUrl = "mongodb://root:example_password@localhost:27017";
        }
        YamlUtils.clearCache();
    }

    public MongoSetup() {
        container.start();
        getCi();
    }

    public static MongoSetup getInstance() {
        if (instance == null) {
            instance = new MongoSetup();
        }

        return instance;
    }

        public MongoDb getMongo() {
        return new MongoDb(
                expectedUrl,
                "test_db");
    }

}
