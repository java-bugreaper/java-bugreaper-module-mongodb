package net.bugreaper.modules.mongodb.setup;

import com.mongodb.ConnectionString;
import com.mongodb.MongoClientSettings;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import net.bugreaper.core.mappers.StringMappers;
import net.bugreaper.modules.mongodb.exceptions.MongoDBHelperException;
import net.bugreaper.modules.mongodb.logger.Log;
import net.bugreaper.modules.mongodb.matcher.JsonMatcher;
import org.apache.commons.lang3.StringUtils;
import org.bson.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static net.bugreaper.core.allurereporter.AllureReporter.attachFromList;
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
                                .maxSize(5)
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

    protected void assertRecordExists(String collectionName,
                                      String json,
                                      boolean strict) {

        Document expected = Document.parse(json);
        MongoCollection<Document> collection = getCollection(collectionName);
        List<String> errors = new ArrayList<>();

        if (Log.LOGGER.isDebugEnabled()) {
            Log.LOGGER.debug("In collection <{}> fount {} records", collectionName, collection.countDocuments());
        }

        int cnt = 0;
        for (Document actual : collection.find()) {
            try {
                cnt++;
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

}
