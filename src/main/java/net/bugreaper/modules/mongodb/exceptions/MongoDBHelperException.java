package net.bugreaper.modules.mongodb.exceptions;

/**
 * Exceptions that can be thrown during MongoDB helper use
 */
public class MongoDBHelperException extends RuntimeException {

    /**
     * Constructs a new exception with the specified detail message
     *
     * @param message String with message
     */
    public MongoDBHelperException(String message) {
        super(message);
    }

}
