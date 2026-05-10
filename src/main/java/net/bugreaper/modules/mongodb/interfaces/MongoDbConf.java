package net.bugreaper.modules.mongodb.interfaces;

import net.bugreaper.modules.mongodb.MongoDb;

/**
 * Interface that defines helper configuration methods for helper operations.
 * Validates that all required methods are implemented.
 */
public interface MongoDbConf {

    /**
     * Configure global await for asserts with await
     *
     * @param awaitMs ms await. Must be >= 200
     * @return this instance for method chaining
     * @throws IllegalArgumentException if {@code awaitMs} is less than 200
     */
    MongoDb setAwaitMs(int awaitMs);

    /**
     * Sets the maximum number of last records to be stored for asserts (and other methods).
     *
     * @param maxLastRecords the maximum number of last records to store. Must be >= 1.
     * @return this instance for method chaining
     * @throws IllegalArgumentException if {@code maxLastRecords} is less than 1
     */
    MongoDb setMaxLastRecords(int maxLastRecords);

    /**
     * Returns and logs (at INFO level) a human-readable summary of all resolved
     * configuration values.
     * <p>
     * The summary includes values loaded from the YAML configuration file as well as
     * any fields overridden programmatically after construction. Optional fields that
     * were not present in the configuration and resolved via default values may also
     * be included.
     *
     * @return String with summary
     */
    String getConfigSummary();
}
