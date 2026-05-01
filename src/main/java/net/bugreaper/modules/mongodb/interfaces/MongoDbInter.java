package net.bugreaper.modules.mongodb.interfaces;


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

}
