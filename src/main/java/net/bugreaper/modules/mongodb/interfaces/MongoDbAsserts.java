package net.bugreaper.modules.mongodb.interfaces;


/**
 * Interface describes methods responsible for helper assertions.
 * Validates that all required methods are implemented.
 */
public interface MongoDbAsserts {

    /**
     * Asserts that the number of documents in the collection is exactly the expected count.
     * <p><b>Uses await.</b></p>
     *
     * @param collectionName collection name
     * @param expectedCount  expected number of documents
     * @throws AssertionError if the assertion fails
     */
    void seeDocumentsCountInCollectionExactly(String collectionName, int expectedCount);

    /**
     * Asserts that the number of documents in the collection is greater than the specified minimum count.
     *
     * <p><b>Uses await.</b></p>
     *
     * @param collectionName collection name
     * @param minCount       minimum expected number of documents
     * @throws AssertionError if the assertion fails
     */
    void seeDocumentsCountInCollectionIsGreaterThan(String collectionName, int minCount);


    /**
     * Asserts that the collection is empty.
     * <p><b>Uses await.</b></p>
     *
     * @param collectionName collection name
     * @throws AssertionError if the assertion fails
     */
    void seeCollectionIsEmpty(String collectionName);

    /**
     * Asserts that the collection is not empty
     * <p><b>Uses await.</b></p>
     *
     * @param collectionName collection name
     * @throws AssertionError if the assertion fails
     */
    void seeCollectionIsNotEmpty(String collectionName);

    /**
     * Asserts that at least one document contains the expected JSON without strict array ordering.
     *
     * <p><b>Uses await until at least one document exists in the collection.</b></p>
     *
     * <p><b>Extra fields and array elements are ignored.</b></p>
     *
     * <p>Wrap this method with the collection name to provide only the JSON data.</p>
     *
     * <p>The maximum number of documents to check is configured globally.
     * Only the latest documents are checked, sorted from newest to oldest.</p>
     *
     * <p>Usage Example:</p>
     * <pre>{@code
     * mongo.seeRecordPartExistsInCollection(
     *       "users",
     *       """
     *       {
     *           "user": {
     *              "name": "Alex",
     *              "array": ["27", "26"]
     *           }
     *       }""");
     * }</pre>
     *
     * @param collectionName collection name
     * @param json           document in JSON format
     * @throws AssertionError if the assertion fails
     */
    void seeDocumentPartExistsInCollection(String collectionName, String json);

    /**
     * Asserts that at least one document is strictly equal to the expected JSON with strict array ordering.
     *
     * <p><b>Uses await until at least one document exists in the collection.</b></p>
     *
     * <p><b>Extra fields and array elements are not expected.</b></p>
     *
     * <p>MongoDB auto-generated field {@code _id} is ignored.</p>
     *
     * <p>Wrap this method with the collection name to provide only JSON data.</p>
     *
     * <p>The maximum number of documents to check is configured globally.
     * Only the latest documents are checked, sorted from newest to oldest.</p>
     *
     * <p>Usage Example:</p>
     * <pre>{@code
     * mongo.seeRecordExistsInCollection(
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
     * @throws AssertionError if the assertion fails
     */
    void seeDocumentExistsInCollection(String collectionName, String json);

}
