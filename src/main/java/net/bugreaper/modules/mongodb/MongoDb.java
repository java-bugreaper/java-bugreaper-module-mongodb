package net.bugreaper.modules.mongodb;

import io.qameta.allure.Param;
import io.qameta.allure.Step;
import net.bugreaper.core.config.YamlUtils;
import net.bugreaper.modules.mongodb.interfaces.MongoDbAsserts;
import net.bugreaper.modules.mongodb.interfaces.MongoDbConf;
import net.bugreaper.modules.mongodb.interfaces.MongoDbInter;
import net.bugreaper.modules.mongodb.logger.Log;
import net.bugreaper.modules.mongodb.setup.MongoDbAbstract;
import org.awaitility.core.ConditionTimeoutException;
import org.bson.Document;

import java.text.MessageFormat;

import static io.qameta.allure.model.Parameter.Mode.HIDDEN;
import static net.bugreaper.core.allurereporter.AllureReporter.attachCanBeNull;
import static net.bugreaper.core.assertions.Asserts.assertGreaterThanExpected;
import static net.bugreaper.core.assertions.Asserts.assertIntEquals;
import static net.bugreaper.core.mappers.StringMappers.formatMilliseconds;
import static net.bugreaper.core.utils.AwaitUtils.awaitCustom;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * Class consists methods that operate with MongoDb
 *
 * <p>For one instance run recommended: {@code MongoDb mongo = MongoDb.getInstance()}</p>
 *
 *
 * <p> Await for some asserts default: {@link #awaitMs}, can be changed by: {@link #setAwaitMs(int)}
 *
 * @author Oleksii Betin "ambu550"
 * @since 1.0.0
 */
@SuppressWarnings("squid:S5960")
public class MongoDb extends MongoDbAbstract implements MongoDbInter, MongoDbAsserts, MongoDbConf {

    private static MongoDb instance;


    public MongoDb(String connectionString, String dbName) {
        super(connectionString, dbName);
    }

    /**
     * Run {@link #MongoDb()} from config in one instance
     */
    public static MongoDb getInstance() {
        if (instance == null) {
            instance = new MongoDb();
        }

        return instance;
    }

    /**
     * Constructs a MongoDb client configuration.
     *
     * <p>Loads configuration values from a YAML file.</p>
     *
     * <p><b>Default file:</b> {@code bugreaper.yml}</p>
     * <p><b>Custom file:</b> using {@code -DbugreaperEnv=test} loads {@code bugreaper-test.yml}</p>
     *
     * <p><b>Required configuration keys:</b></p>
     * <ul>
     *     <li>{@code modules.mongodb.url}</li>
     *     <li>{@code modules.mongodb.database}</li>
     * </ul>
     *
     * <p><b>Optional configuration keys:</b></p>
     * <ul>
     *     <li>{@code modules.mongodb.await}</li>
     * </ul>
     *
     * <p>Missing required keys will result in configuration errors.
     * Missing optional keys will fall back to predefined defaults.</p>
     */
    public MongoDb() {
        //required config fields
        super(YamlUtils.getStringValueByPath("modules.mongodb.url"),
                YamlUtils.getStringValueByPath("modules.mongodb.database"));

        //optional config fields
        Object awaitVal = YamlUtils.getValueByPath("modules.mongodb.await", true);
        if (awaitVal instanceof Number number) {
            setAwaitMs(number.intValue());
        }
    }

    // Setters/getters

    @Override
    public MongoDb setAwaitMs(int awaitMs) {
        if (awaitMs < 200) {
            throw new IllegalArgumentException("awaitMs too small (can`t bee less 200ms)");
        }
        this.awaitMs = awaitMs;
        return this;
    }

    @Override
    public String getConfigSummary() {
        String info = String.format("""
                        %s:
                            url=%s
                            default_database=%s
                            awaitMs=%d%n""",
                this.getClass().getSimpleName(), connectionString, defaultDatabase.getName(), awaitMs);

        Log.LOGGER.info(info);
        return info;
    }

    // Interactions

    @Override
    @Step("(MongoDb) Clean collection <{collectionName}>")
    public void cleanCollection(String collectionName) {
        getCollection(collectionName).deleteMany(new Document());
    }

    @Override
    @Step("(MongoDb) Insert into collection <{collectionName}>")
    public void insertIntoCollection(String collectionName, @Param(mode = HIDDEN) String json) {
        attachCanBeNull("add record:", json);
        Document doc = Document.parse(json);
        getCollection(collectionName).insertOne(doc);
    }

    // Get


    @Override
    // no step
    public int getRecordsCountInCollection(String collectionName) {
        return (int) getCollection(collectionName).countDocuments();
    }

    // Asserts
    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> has exactly {expectedCount} records")
    public void seeRecordsCountInCollectionExactly(String collectionName, int expectedCount) {

        try {
            awaitCustom(awaitMs).untilAsserted(() ->
                    assertIntEquals(expectedCount, getRecordsCountInCollection(collectionName)));

        } catch (ConditionTimeoutException e) {
            fail(
                    MessageFormat.format(
                            "Count records from collection <{0}> expected to be EXACTLY <{1}> but got <{2}> within {3}",
                            collectionName, expectedCount, getRecordsCountInCollection(collectionName), formatMilliseconds(awaitMs)));
        }

    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> is not empty")
    public void seeCollectionIsEmpty(String collectionName) {

        try {
            awaitCustom(awaitMs).untilAsserted(() ->
                    assertIntEquals(0, getRecordsCountInCollection(collectionName)));

        } catch (ConditionTimeoutException e) {
            fail(
                    MessageFormat.format(
                            "Collection <{0}> expected to be empty but got <{1}> records within {2}",
                            collectionName, getRecordsCountInCollection(collectionName), formatMilliseconds(awaitMs)));
        }

    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> is empty")
    public void seeCollectionIsNotEmpty(String collectionName) {

        try {
            awaitCustom(awaitMs).untilAsserted(() ->
                    assertGreaterThanExpected(0, getRecordsCountInCollection(collectionName)));
        } catch (ConditionTimeoutException e) {
            fail(
                    MessageFormat.format(
                            "Collection <{0}> expected to be not empty but got no records within {1}",
                            collectionName, formatMilliseconds(awaitMs)));
        }

    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> has record CONTAINS JSON")
    public void seeRecordPartExistsInCollection(String collectionName, @Param(mode = HIDDEN) String json) {
        attachCanBeNull("Expected part:", json);
        assertRecordExists(collectionName, json, false);
    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> has record EQUAL to JSON")
    public void seeRecordExistsInCollection(String collectionName, @Param(mode = HIDDEN) String json) {
        attachCanBeNull("Expected:", json);
        assertRecordExists(collectionName, json, true);
    }

}
