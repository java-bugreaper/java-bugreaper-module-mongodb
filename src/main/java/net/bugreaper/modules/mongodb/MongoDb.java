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
 * MongoDB helper that provides a common API for operating with MongoDB.
 *
 * <p>Recommended to use one instance:
 * {@code MongoDb mongo = MongoDb.getInstance();}
 * </p>
 *
 * <p>Default await timeout for assertions with await is configured by {@link #awaitMs}.
 * It can be changed using {@link #setAwaitMs(int)} or configuration.</p>
 *
 * @author Oleksii Betin "ambu550"
 * @since 1.0.0
 */
public class MongoDb extends MongoDbAbstract implements MongoDbInter, MongoDbAsserts, MongoDbConf {

    private static MongoDb instance;

    /**
     * specific ms await in specific assert (configure with {@link #withAwaitMs(int)})
     */
    private final ThreadLocal<Integer> specificAwaitMs = ThreadLocal.withInitial(() -> 0);


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
     * Constructs a MongoDb client using YAML configuration.
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
     *     await: 420 # (optional) await timeout in milliseconds
     *     documents-max-count: 15 # (optional) maximum number of documents to check
     * </pre>
     *
     * <p>Missing required keys will result in configuration errors.
     * Missing optional keys will fall back to predefined defaults.</p>
     *
     * @throws IllegalArgumentException if the configuration contains invalid values
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
    public MongoDb withAwaitMs(int specificAwaitMs) {
        if (specificAwaitMs < 200) {
            throw new IllegalArgumentException("specificAwaitMs too small (can`t bee less 200ms)");
        }
        this.specificAwaitMs.set(specificAwaitMs);
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
    public MongoDb setTemplatesDirectory(String templatesPath) {
        if (templatesPath == null || templatesPath.isBlank()) {
            throw new IllegalArgumentException("templatesPath can`t be empty or null");
        }
        this.templatesPath = templatesPath;
        return this;
    }

    @Override
    public String getConfigSummary() {
        String info = String.format("""
                        %s:
                            url=%s
                            default_database=%s
                            awaitMs=%d
                            maxLastRecords=%d
                            templatesPath=%s%n""",
                this.getClass().getSimpleName(), connectionString, defaultDatabase.getName(), awaitMs, maxLastRecords, templatesPath);

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

    @Override
    @Step("(MongoDb) Insert into collection <{collectionName}>")
    public void insertTemplateIntoCollection(String collectionName, String providedJson) {
        insertIntoCollectionTemplateMethod(collectionName, providedJson);
    }

    // Get


    @Override
    // no step
    public int getDocumentsCountInCollection(String collectionName) {
        return getRecordsCountInCollectionMethod(collectionName);
    }

    @Override
    @Step("(MongoDb) Grab documents from collection: {collectionName}")
    public AssertableStringList grabDocumentsFromCollection(String collectionName) {
        return grabDocumentsFromCollectionMethod(collectionName, await());
    }

    // Asserts

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> has exactly {expectedCount} documents")
    public void seeDocumentsCountInCollectionExactly(String collectionName, int expectedCount) {
        seeRecordsCountInCollectionExactlyMethod(collectionName, expectedCount, await());
    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> has greater than {minCount} documents")
    public void seeDocumentsCountInCollectionIsGreaterThan(String collectionName, int minCount) {
        seeRecordsCountInCollectionIsGreaterThanMethod(collectionName, minCount, await());
    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> is not empty")
    public void seeCollectionIsEmpty(String collectionName) {
        seeCollectionIsEmptyMethod(collectionName, await());
    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> is empty")
    public void seeCollectionIsNotEmpty(String collectionName) {
        seeCollectionIsNotEmptyMethod(collectionName, await());
    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> has a document CONTAINS JSON")
    public void seeDocumentPartExistsInCollection(String collectionName, @Param(mode = HIDDEN) String json) {
        attachCanBeNull("Expected part:", json);
        assertRecordExists(collectionName, json, false, await());
    }

    @Override
    @Step("(MongoDb)[ASSERT] Collection: <{collectionName}> has a document EQUAL to JSON")
    public void seeDocumentExistsInCollection(String collectionName, @Param(mode = HIDDEN) String json) {
        attachCanBeNull("Expected:", json);
        assertRecordExists(collectionName, json, true, await());
    }

    private int await() {
        if (specificAwaitMs.get() != 0) {
            int result = specificAwaitMs.get();
            specificAwaitMs.remove();
            return result;
        } else {
            return awaitMs;
        }
    }
}
