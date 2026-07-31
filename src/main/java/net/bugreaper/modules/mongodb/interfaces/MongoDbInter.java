package net.bugreaper.modules.mongodb.interfaces;

import net.bugreaper.core.assertable.AssertableStringList;
import net.bugreaper.core.mappers.JsonMerge;

/**
 * Interface defines methods for facilitating helper interactions.
 * Validates that all required methods are implemented.
 */
public interface MongoDbInter {

    /**
     * Deletes all documents from the collection.
     *
     * @param collectionName collection name
     */
    void cleanCollection(String collectionName);

    /**
     * Inserts a document into the collection.
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
     * @param json           document in JSON format
     */
    void insertIntoCollection(String collectionName, String json);

    /**
     * Performs a recursive (deep) merge of JSON templates and inserts the resulting document into the collection.
     *
     * <pre>
     * Example:
     * collectionName: "myCollection"
     *
     * The method searches for the template file:
     * templates/mongodb/myCollection.json
     *
     * If a database name is provided with the collection, use the same filename format:
     * "mydb.myCollection.json"
     *
     * The default template directory can be changed using the setter.
     * </pre>
     *
     * <p>Same behavior as: <a href="https://bug-reaper.gitlab.io/java-bugreaper-core/apidocs/net/bugreaper/core/mappers/JsonMerge.html#mergeJsonDeep(java.lang.String,java.lang.String)">mergeJsonDeep</a>
     *
     * @param collectionName collection name
     * @param providedJson   document in JSON format
     * @see JsonMerge#mergeJsonDeep(String, String) for deep merge logic
     */
    void insertTemplateIntoCollection(String collectionName, String providedJson);

    /**
     * Returns the number of documents in the collection.
     *
     * @param collectionName collection name
     * @return number of documents in the collection
     */
    int getDocumentsCountInCollection(String collectionName);

    /**
     * Grabs documents from the collection into a list.
     *
     * <p>The maximum number of documents is configured globally.
     * Documents are sorted from newest to oldest before returning.</p>
     *
     * <p><b>Uses await until at least one document exists in the collection.</b></p>
     *
     * @param collectionName collection name
     * @return {@link AssertableStringList} containing the grabbed documents
     * @throws AssertionError if the collection is empty
     *
     *                        <p> EXAMPLE:
     *                        {@code grabDocumentsFromCollection("my_collection").seeListHasExactlyCount(4); }
     */
    AssertableStringList grabDocumentsFromCollection(String collectionName);

}
