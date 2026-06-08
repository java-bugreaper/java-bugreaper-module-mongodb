package net.bugreaper.modules.mongodb.setup;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
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

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.mongodb.client.model.Sorts.descending;
import static net.bugreaper.core.allurereporter.AllureReporter.attachFromList;
import static net.bugreaper.core.assertions.Asserts.assertGreaterThanExpected;
import static net.bugreaper.core.assertions.Asserts.assertIntEquals;
import static net.bugreaper.core.mappers.StringMappers.formatMilliseconds;
import static net.bugreaper.core.mappers.StringMappers.listToString;
import static net.bugreaper.core.utils.AwaitUtils.awaitCustom;
import static org.junit.jupiter.api.Assertions.fail;

@SuppressWarnings("squid:S5960")
public abstract class MongoDbAbstract {

    protected MongoClient client;
    protected final String connectionString;
    protected MongoDatabase defaultDatabase;
    /**
     * default ms await in tests
     */
    protected int awaitMs = 2000;

    /**
     * Default pagination limit for retrieving the N most recent records from the end of a collection.
     * This value is used to optimize test data assertions, grab data, show data.
     * Increasing this limit may affect performance if used in high-throughput queries within test suites.
     */
    protected int maxLastRecords = 50;

    protected MongoDbAbstract(String connectionString, String dbName) {

        this.connectionString = connectionString;

        // default connection settings
        MongoClientSettings settings = MongoClientSettings.builder()
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

        client = MongoClients.create(settings);

        defaultDatabase = client.getDatabase(dbName);

        // shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (client != null) {
                client.close();
                Log.LOGGER.debug(("MongoClient closed"));
            }
        }));
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

    protected AssertableStringList grabDocumentsFromCollectionMethod(String collectionName) {

        seeCollectionIsNotEmptyMethod(collectionName);
        preCheckDocumentsCount(collectionName);

        JsonWriterSettings pretty = JsonWriterSettings.builder()
                .indent(true)
                .outputMode(JsonMode.RELAXED)
                .build();

        List<String> actualList = new ArrayList<>();

        getCollection(collectionName)
                .find()
                .sort(descending("_id")) // get latest first
                .limit(maxLastRecords)
                .forEach(doc -> actualList.add(doc.toJson(pretty)));

        if (Log.LOGGER.isDebugEnabled()) {
            Log.LOGGER.debug("List of messages: {}", listToString(actualList));
        }

        Log.LOGGER.info("Documents grabbed from collection <{}>: {}", collectionName, actualList.size());
        attachFromList(String.format("Documents(%d) list:", actualList.size()), actualList);

        return new AssertableStringList(actualList);
    }

    protected void assertRecordExists(String collectionName,
                                      String json,
                                      boolean strict) {

        Document expected = Document.parse(json);
        MongoCollection<Document> collection = getCollection(collectionName);
        List<String> errors = new ArrayList<>();



        if (Log.LOGGER.isInfoEnabled()) {
            Log.LOGGER.info("In collection <{}> found {} documents", collectionName, collection.countDocuments());
        }

        preCheckDocumentsCount(collectionName);

        int cnt = 0;
        // get latest first
        for (Document actual : collection.find().sort(descending("_id")).limit(maxLastRecords) ) {
            try {
                cnt++;

                Log.LOGGER.debug("Document for check:\n{}", actual);

                JsonMatcher.assertMatches(cnt, expected, actual, strict);
                return;
            } catch (AssertionError error) {
                errors.add(error.getMessage());
            }
        }

        String str = "CONTAINS";
        if (strict) {
            str = "STRICT";
        }

        //Allure attach
        if (cnt != 0) {
            attachFromList("Differences:", errors);
        }

        fail(
                String.format("No %s matching found in collection <%s> %d actual documents for:%n%s%nDifferences:%n%s",
                        str, collectionName, cnt, json, StringMappers.listToString(errors)));

    }

    protected int getRecordsCountInCollectionMethod(String collectionName){
        return (int) getCollection(collectionName).countDocuments();
    }

    protected void seeRecordsCountInCollectionExactlyMethod(String collectionName, int expectedCount) {

        try {
            awaitCustom(awaitMs).untilAsserted(() ->
                    assertIntEquals(expectedCount, getRecordsCountInCollectionMethod(collectionName)));

        } catch (ConditionTimeoutException e) {
            fail(
                    MessageFormat.format(
                            "Count records from collection <{0}> expected to be EXACTLY <{1}> but got <{2}> within {3}",
                            collectionName, expectedCount, getRecordsCountInCollectionMethod(collectionName), formatMilliseconds(awaitMs)));
        }

    }

    protected void seeCollectionIsEmptyMethod(String collectionName) {

        try {
            awaitCustom(awaitMs).untilAsserted(() ->
                    assertIntEquals(0, getRecordsCountInCollectionMethod(collectionName)));

        } catch (ConditionTimeoutException e) {
            fail(
                    MessageFormat.format(
                            "Collection <{0}> expected to be empty but got <{1}> records within {2}",
                            collectionName, getRecordsCountInCollectionMethod(collectionName), formatMilliseconds(awaitMs)));
        }

    }

    protected void seeCollectionIsNotEmptyMethod(String collectionName) {

        try {
            awaitCustom(awaitMs).untilAsserted(() ->
                    assertGreaterThanExpected(0, getRecordsCountInCollectionMethod(collectionName)));
        } catch (ConditionTimeoutException e) {
            fail(
                    MessageFormat.format(
                            "Collection <{0}> expected to be not empty but got no records within {1}",
                            collectionName, formatMilliseconds(awaitMs)));
        }

    }


    private void preCheckDocumentsCount(String collectionName){

        int cnt =getRecordsCountInCollectionMethod(collectionName);

        if(cnt > maxLastRecords){
            Log.LOGGER.warn("""
                    Count of documents in collection <{}>={}: more than maxLastRecords({}) in config
                    only last documents will be taken into account (can be changed by .setMaxLastRecords(int) or config 'documents-max-count')""", collectionName, cnt, maxLastRecords);
        }
    }
}
