package net.bugreaper.modules.mongodb.setup;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Sorts;
import net.bugreaper.core.assertable.AssertableStringList;
import net.bugreaper.core.mappers.StringMappers;
import net.bugreaper.modules.mongodb.exceptions.MongoDBHelperException;
import net.bugreaper.modules.mongodb.logger.Log;
import net.bugreaper.modules.mongodb.matcher.JsonMatcher;
import org.apache.commons.lang3.StringUtils;
import org.awaitility.core.ConditionTimeoutException;
import org.bson.Document;
import org.bson.json.JsonMode;
import org.bson.json.JsonWriterSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.mongodb.client.model.Sorts.descending;
import static net.bugreaper.core.allurereporter.AllureReporter.attachCanBeNull;
import static net.bugreaper.core.allurereporter.AllureReporter.attachFromList;
import static net.bugreaper.core.filereaders.FileReader.readJsonFromFile;
import static net.bugreaper.core.mappers.JsonMerge.mergeJsonDeep;
import static net.bugreaper.core.mappers.StringMappers.formatMilliseconds;
import static net.bugreaper.core.mappers.StringMappers.listToString;
import static net.bugreaper.core.utils.AwaitUtils.*;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

@SuppressWarnings("squid:S5960")
public abstract class MongoDbAbstract {

    protected final String connectionString;

    protected MongoClient client;
    protected MongoDatabase defaultDatabase;

    /**
     * Default await timeout for tests, in milliseconds.
     */
    protected volatile int awaitMs = 2000;

    /**
     * Default await polling interval in milliseconds for tests.
     */
    protected volatile int awaitPollInterval = 100;

    /**
     * Default pagination limit for retrieving the N most recent documents from the end of a collection.
     * This value is used to optimize test data assertions, grab data, show data.
     * Increasing this limit may affect performance if used in high-throughput queries within test suites.
     */
    protected volatile int maxLastDocuments = 50;

    /**
     * Default templates path for insert
     */
    protected volatile String templatesPath = "templates/mongodb/";

    private static final String SUBJECT = "documents";

    private static final String CONTAINER = "collection";


    protected MongoDbAbstract(String connectionString, String dbName) {

        this.connectionString = connectionString;
        client = MongoClients.create(setupSettings());

        defaultDatabase = client.getDatabase(dbName);

        Runtime.getRuntime().addShutdownHook(createShutdownHook());
    }

    Thread createShutdownHook() {
        return new Thread(() -> {
            if (client != null) {
                client.close();
                Log.LOGGER.debug(("MongoClient closed"));
            }
        }, "mongodb-connection-shutdown");
    }

    private MongoClientSettings setupSettings() {

        // default connection settings
        return MongoClientSettings.builder()
                .applyToClusterSettings(builder ->
                        builder.serverSelectionTimeout(5000, TimeUnit.MILLISECONDS))
                .applyConnectionString(new ConnectionString(connectionString))
                .applyToSocketSettings(builder ->
                        builder.connectTimeout(2000, TimeUnit.MILLISECONDS))
                .applyToConnectionPoolSettings(builder ->
                        builder
                                .maxSize(10)
                                .minSize(2)
                                .maxWaitTime(2000, TimeUnit.MILLISECONDS)
                )
                .build();
    }

    private MongoDatabase getDb(String dbName) {
        return client.getDatabase(dbName);
    }


    protected MongoCollection<Document> getCollection(String collectionName) {

        if (collectionName == null || collectionName.isEmpty()) {
            throw new MongoDBHelperException("Collection can't be null or empty");
        }

        //check is database provided
        if (collectionName.indexOf('.') == -1) {
            //use default database
            return defaultDatabase.getCollection(collectionName);
        }

        //use provided database
        return getDb(StringUtils.substringBefore(collectionName, "."))
                .getCollection(StringUtils.substringAfter(collectionName, "."));
    }

    protected AssertableStringList grabDocumentsFromCollectionMethod(String collectionName, int providedAwaitMs) {

        waitForFirsDocument(collectionName, providedAwaitMs);
        preCheckDocumentsCount(collectionName);

        JsonWriterSettings pretty = JsonWriterSettings.builder()
                .indent(true)
                .outputMode(JsonMode.RELAXED)
                .build();

        List<String> actualList = new ArrayList<>();

        getCollection(collectionName)
                .find()
                .sort(descending("_id")) // get latest first
                .limit(maxLastDocuments)
                .forEach(doc -> actualList.add(doc.toJson(pretty)));

        if (Log.LOGGER.isDebugEnabled()) {
            Log.LOGGER.debug("List of messages:\n{}", listToString(actualList));
        }

        Log.LOGGER.info("Documents grabbed from collection '{}': {}", collectionName, actualList.size());
        attachFromList(String.format("Documents(%d) list:", actualList.size()), actualList);

        return new AssertableStringList(actualList);
    }

    protected void assertRecordExists(String collectionName,
                                      String json,
                                      boolean strict,
                                      int providedAwaitMs) {

        Document expected = Document.parse(json);

        AtomicReference<List<String>> lastErrors =
                new AtomicReference<>(new ArrayList<>());

        AtomicInteger checkedRecords =
                new AtomicInteger();

        try {
            awaitCustom(providedAwaitMs, awaitPollInterval).until(() -> {

                List<String> errors = new ArrayList<>();

                boolean found = checkRecords(
                        collectionName,
                        expected,
                        strict,
                        errors,
                        checkedRecords
                );

                lastErrors.set(errors);

                //no warning log that read only limit!

                return found;
            });

        } catch (ConditionTimeoutException e) {

            if (lastErrors.get().isEmpty()) {
                throw new AssertionError(
                        "Collection '%s' contains no documents within %s"
                                .formatted(collectionName, formatMilliseconds(providedAwaitMs)));
            }


            String mode = strict ? "STRICT" : "CONTAINS";

            if (!lastErrors.get().isEmpty()) {
                attachFromList("Differences:", lastErrors.get());
            }

            //on fail with difference only!
            preCheckDocumentsCount(collectionName);

            throw new AssertionError(
                    String.format(
                            """
                                    No %s matching document found in collection '%s' within %s
                                    Checked documents: %d
                                        
                                    Expected:
                                    %s
                                        
                                    Differences:
                                    %s
                                    """,
                            mode,
                            collectionName,
                            formatMilliseconds(providedAwaitMs),
                            checkedRecords.get(),
                            json,
                            StringMappers.listToString(lastErrors.get())
                    )
            );
        }
    }

    private boolean checkRecords(String collectionName,
                                 Document expected,
                                 boolean strict,
                                 List<String> errors,
                                 AtomicInteger checkedRecords) {

        List<Document> records = getCollection(collectionName)
                .find()
                .sort(Sorts.descending("_id"))
                .limit(maxLastDocuments)
                .into(new ArrayList<>());

        checkedRecords.set(records.size());

        // Check newest -> oldest
        for (int i = 0; i < records.size(); i++) {

            try {

                JsonMatcher.assertMatches(
                        i + 1,
                        expected,
                        records.get(i),
                        strict
                );

                return true;

            } catch (AssertionError e) {
                errors.add(e.getMessage());
            }
        }

        return false;
    }

    protected void insertIntoCollectionMethod(String collectionName, String json) {
        attachCanBeNull("add document:", json);

        try {
            Document doc = Document.parse(json);
            getCollection(collectionName).insertOne(doc);
        } catch (Exception e) {
            throw new MongoDBHelperException("Failed to insert document: " + e.getMessage(), e);
        }

    }

    protected void insertIntoCollectionTemplateMethod(String collectionName, String providedJson) {

        String fullTemplatePath = templatesPath + collectionName + ".json";
        Log.LOGGER.debug("Looking for template file in resources: {}", fullTemplatePath);

        String finalJson = mergeJsonDeep(readJsonFromFile(fullTemplatePath), providedJson);

        insertIntoCollectionMethod(collectionName, finalJson);
    }

    protected int getRecordsCountInCollectionMethod(String collectionName) {
        return (int) getCollection(collectionName).countDocuments();
    }

    protected void seeRecordsCountInCollectionExactlyMethod(String collectionName, int expectedCount, int providedAwaitMs) {
        awaitEquals(expectedCount, () -> getRecordsCountInCollectionMethod(collectionName), providedAwaitMs, awaitPollInterval, SUBJECT, CONTAINER, collectionName);
    }

    protected void seeRecordsCountInCollectionIsGreaterThanMethod(String collectionName, int minCount, int providedAwaitMs) {
        awaitGraterThan(minCount, () -> getRecordsCountInCollectionMethod(collectionName), providedAwaitMs, awaitPollInterval, SUBJECT, CONTAINER, collectionName);
    }

    protected void seeCollectionIsEmptyMethod(String collectionName, int providedAwaitMs) {
        awaitIsEmpty(() -> getRecordsCountInCollectionMethod(collectionName), providedAwaitMs, awaitPollInterval, SUBJECT, CONTAINER, collectionName);
    }

    private void waitForFirsDocument(String collectionName, int providedAwaitMs) {
        try {
            awaitCustom(providedAwaitMs, awaitPollInterval).untilAsserted(() ->
                    assertNotEquals(0, getRecordsCountInCollectionMethod(collectionName)));
        } catch (ConditionTimeoutException e) {
            throw new ConditionTimeoutException(
                    "No documents were received from collection '%s' within %s"
                            .formatted(collectionName, formatMilliseconds(providedAwaitMs)));
        }
    }

    protected void seeCollectionIsNotEmptyMethod(String collectionName, int providedAwaitMs) {
        awaitIsNotEmpty(() -> getRecordsCountInCollectionMethod(collectionName), providedAwaitMs, awaitPollInterval, SUBJECT, CONTAINER, collectionName);
    }


    private void preCheckDocumentsCount(String collectionName) {

        int cnt = getRecordsCountInCollectionMethod(collectionName);

        if (cnt > maxLastDocuments) {
            Log.LOGGER.warn("""
                    Number of documents in collection '{}' is <{}>: more than maxLastRecords({}) in config
                    only last documents will be taken into account (can be changed by .setMaxLastRecords(int) or config 'documents-max-count')""", collectionName, cnt, maxLastDocuments);
        }
    }
}
