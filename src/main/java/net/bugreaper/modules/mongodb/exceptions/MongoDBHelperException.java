package net.bugreaper.modules.mongodb.exceptions;

/**
 * Exceptions that can be thrown during MongoDB helper use
 */
public class MongoDBHelperException extends RuntimeException {

    /**
     * Basic exception for MongoDb helper
     *
     * @param message String with message
     */
    public MongoDBHelperException(String message) {
        super(message);
    }

    /**
     * Basic exception for MongoDb helper
     *
     * @param message A descriptive message explaining the error
     * @param cause The underlying cause that triggered this exception
     */
    public MongoDBHelperException(String message, Throwable cause) {
        super(message, cause);
    }

}
