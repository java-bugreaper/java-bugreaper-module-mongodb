package net.bugreaper.modules.mongodb.interfaces;

import net.bugreaper.core.assertable.AssertableStringList;
import net.bugreaper.core.assertions.JsonAsserts;
import net.bugreaper.core.mappers.JsonMerge;

/**
 * Interface defines methods for facilitating helper interactions.
 * Validates that all required methods are implemented.
 */
public interface MongoDbInter {

    /**
     * Delete all records in collection
     *
     * @param collectionName collection name
     */
    void cleanCollection(String collectionName);

    /**
     * Insert record to collection
     * <p>Wrap this method with collection name to provide only json</p>
     * * <p>Same behavior as {@link JsonAsserts#assertJsonsExtended(String, String)}.
     * <p>Usage Example:</p>
     * <pre>{@code
     * mongo.insertIntoCollection(
     *       "users",
     *       """
     *       {
     *           "user": {
     *              "name": "Alex",
     *              "age": 33,
     *              "array": ["25", "26", "27"]
     *           }
     *       }""");
     * }</pre>
     *
     * @param collectionName collection name
     * @param json           record in Json format
     */
    void insertIntoCollection(String collectionName, String json);

    /**
     * Performs a recursive (deep) merge Jsons and insert record to collection
     * <pre>
     * example: collectionName: "myCollection"
     * (will search in resources file templates/mongodb/myCollection.json)
     * if provide collection with database create same filename!!("mydb.myCollection.json")
     * default template directory can be changed by setter
     * </pre>
     *
     * <p>Same behavior as: <a href="https://bug-reaper.gitlab.io/java-bugreaper-core/apidocs/net/bugreaper/core/mappers/JsonMerge.html#mergeJsonDeep(java.lang.String,java.lang.String)">mergeJsonDeep</a>
     *
     * @param collectionName collection name
     * @param providedJson   record in Json format
     * @see JsonMerge#mergeJsonDeep(String, String) merge logic
     */
    void insertTemplateIntoCollection(String collectionName, String providedJson);

    /**
     * Return records count in collection
     *
     * @param collectionName collection name
     * @return int with messages count
     */
    int getRecordsCountInCollection(String collectionName);

    /**
     * Grab documents to list
     * <p>max count of documents set in config (grab last documents sorted from the latest to the oldest)</p>
     * <p><b>await for at least one Document exist in collection</b>
     *
     * @param collectionName collection name
     * @return {@link AssertableStringList}
     * @throws AssertionError if collection is empty
     *
     *                        <p> EXAMPLE:
     *                        {@code grabDocumentsFromCollection("my_collection").seeListHasExactlyCount(4); }
     */
    AssertableStringList grabDocumentsFromCollection(String collectionName);

}
