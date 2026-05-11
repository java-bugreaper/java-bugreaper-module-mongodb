package net.bugreaper.modules.mongodb.interfaces;

import net.bugreaper.core.assertable.AssertableStringList;

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
     *
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
     * @param json     record in Json format
     */
    void insertIntoCollection(String collectionName, String json);

    /**
     * Return records count in collection
     *
     * @param collectionName collection name
     * @return int with messages count
     */
    int getRecordsCountInCollection(String collectionName);

    /**
     * Grab documents to list
     * <p> max count of documents set in config (grab last documents)</p>
     * <p><b>wait for first Document</b>
     *
     * @param collectionName collection name
     * @return  {@link AssertableStringList}
     *
     * <p> EXAMPLE:
     * {@code grabDocumentsFromCollection("my_collection").seeListHasExactlyCount(4); }
     */
    AssertableStringList grabDocumentsFromCollection(String collectionName);

}
