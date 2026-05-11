package net.bugreaper.modules.mongodb.interfaces;


import net.bugreaper.modules.mongodb.matcher.JsonMatcher;

/**
 * Interface describes methods responsible for helper assertions.
 * Validates that all required methods are implemented.
 */
public interface MongoDbAsserts {

   /**
    * Assert that count of records in collection is equal to expected
    * <p><b>with await</b>
    *
    * @param collectionName collection name
    * @param expectedCount expected count
    * @throws AssertionError on assert fail
    */
    void seeRecordsCountInCollectionExactly(String collectionName, int expectedCount);

   /**
    * Assert that collection is empty
    * <p><b>with await</b>
    *
    * @param collectionName collection name
    * @throws AssertionError on assert fail
    */
    void seeCollectionIsEmpty(String collectionName);

   /**
    * Assert that collection is not empty
    * <p><b>with await</b>
    *
    * @param collectionName collection name
    * @throws AssertionError on assert fail
    */
    void seeCollectionIsNotEmpty(String collectionName);

    /**
     * Assert that exists at least one record CONTAINS JSON without strict order
     * <p><b>Extensible fields and elements in array <u>will be skipped</u></b></p>
     * <p>Wrap this method with collection name to provide only json</p>
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
     * @param json     record in Json format
     * @throws AssertionError on assert fail
     */
    void seeRecordPartExistsInCollection(String collectionName, String json);

    /**
     * Assert that exists at least one record STRICT EQUAL to JSON with strict array order
     * <p><b>Extensible fields and elements in arrays <u>not expected</u></b></p>
     * <b>Ignore Mongo auto-generated field {@link JsonMatcher#IGNORED_FIELD}</b>
     * <p>Wrap this method with collection name to provide only json</p>
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
     * @param json     record in Json format
     * @throws AssertionError on assert fail
     */
    void seeRecordExistsInCollection(String collectionName, String json);

}
