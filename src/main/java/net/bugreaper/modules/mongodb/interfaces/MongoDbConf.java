package net.bugreaper.modules.mongodb.interfaces;

import net.bugreaper.modules.mongodb.MongoDb;

/**
 * Interface that defines helper configuration methods for helper operations.
 * Validates that all required methods are implemented.
 */
public interface MongoDbConf {

    /**
     * Configures the global await timeout for assertions and operations that use await.
     *
     * @param awaitMs await timeout in milliseconds
     * @return this instance for method chaining
     * @throws IllegalArgumentException if the provided timeout is invalid or less than 200 milliseconds
     */
    MongoDb setAwaitMs(int awaitMs);

    /**
     * Configures await timeout for the next assertion or operation that uses await.
     *
     * <p>After execution, the await timeout is reset to the global value configured by
     * {@link #setAwaitMs(int)}.</p>
     *
     * <p>The global await timeout is ignored for this operation.</p>
     *
     * @param awaitMs await timeout in milliseconds
     * @return this instance for method chaining
     * @throws IllegalArgumentException if the provided timeout is invalid
     */
    MongoDb withAwaitMs(int awaitMs);

    /**
     * Sets the maximum number of latest documents to check during assertions and other operations.
     *
     * @param maxLastRecords the maximum number of last documents to store. Must be >= 1.
     * @return this instance for method chaining
     * @throws IllegalArgumentException if {@code maxLastRecords} is less than 1
     */
    MongoDb setMaxLastRecords(int maxLastRecords);

    /**
     * Configures the directory in resources containing collection templates.
     *
     * @param templatePath path to the templates directory in resources (example: {@code "my_dir/sub_dir/"})
     * @return this instance for method chaining
     * @throws IllegalArgumentException if the provided path is invalid
     */
    MongoDb setTemplatesDirectory(String templatePath);


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
