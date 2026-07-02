package net.bugreaper.modules.mongodb;

import io.qameta.allure.Param;
import io.qameta.allure.Step;
import net.bugreaper.core.assertable.AssertableStringList;
import net.bugreaper.core.config.YamlUtils;
import net.bugreaper.modules.mongodb.interfaces.MongoDbAsserts;
import net.bugreaper.modules.mongodb.interfaces.MongoDbConf;
import net.bugreaper.modules.mongodb.interfaces.MongoDbInter;
import net.bugreaper.modules.mongodb.logger.Log;
import net.bugreaper.modules.mongodb.setup.MongoDbAbstract;
import org.bson.Document;

import static io.qameta.allure.model.Parameter.Mode.HIDDEN;
import static net.bugreaper.core.allurereporter.AllureReporter.attachCanBeNull;

/**
 * Class consists methods that operate with MongoDb
 *
 * <p>For one instance run recommended: {@code MongoDb mongo = MongoDb.getInstance();}</p>
 *
 *
 * <p> Await for some asserts default: {@link #awaitMs}, can be changed by: {@link #setAwaitMs(int)}
 *
 * @author Oleksii Betin "ambu550"
 * @since 1.0.0
 */
public class MongoDb extends MongoDbAbstract implements MongoDbInter, MongoDbAsserts, MongoDbConf {

    private static MongoDb instance;


    public MongoDb(String connectionString, String dbName) {
        super(connectionString, dbName);
    }

    /**
     * Returns the instance of {@link MongoDb} with config builder {@link #MongoDb()}.
     * <p>
     * This implementation is thread-safe using method-level synchronization.
     *
     * @return the singleton instance of {@link MongoDb}
     * @see #MongoDb() config setup
     */
    public static synchronized MongoDb getInstance() {
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
     * <pre>
     * modules:
     *   mongodb:
     *     url: 'mongodb://root:example_password@localhost:27017'
     *     database: test_db
     *     await: 420 # optional
     *     documents-max-count: 15 # optional
     * </pre>
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
        Object maxDoc = YamlUtils.getValueByPath("modules.mongodb.documents-max-count", true);
        if (maxDoc instanceof Number number) {
            setMaxLastRecords(number.intValue());
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
    public MongoDb setMaxLastRecords(int maxLastRecords) {
        if (maxLastRecords < 1) {
            throw new IllegalArgumentException("maxLastRecords too small (can`t bee less 1)");
        }
        this.maxLastRecords = maxLastRecords;
        return this;
    }

    @Override
    public String getConfigSummary() {
        String info = String.format("""
                        %s:
                            url=%s
                            default_database=%s
                            awaitMs=%d
                            maxLastRecords=%d%n""",
                this.getClass().getSimpleName(), connectionString, defaultDatabase.getName(), awaitMs, maxLastRecords);

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
        insertIntoCollectionMethod(collectionName, json);
    }

    // Get


    @Override
    // no step
    public int getRecordsCountInCollection(String collectionName) {
        return getRecordsCountInCollectionMethod(collectionName);
    }

    @Override
    @Step("(MongoDb) Grab documents from collection: {collectionName}")
    public AssertableStringList grabDocumentsFromCollection(String collectionName) {
        return grabDocumentsFromCollectionMethod(collectionName);
    }

    // Asserts

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> has exactly {expectedCount} records")
    public void seeRecordsCountInCollectionExactly(String collectionName, int expectedCount) {
        seeRecordsCountInCollectionExactlyMethod(collectionName, expectedCount);
    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> is not empty")
    public void seeCollectionIsEmpty(String collectionName) {
        seeCollectionIsEmptyMethod(collectionName);
    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> is empty")
    public void seeCollectionIsNotEmpty(String collectionName) {
        seeCollectionIsNotEmptyMethod(collectionName);
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
