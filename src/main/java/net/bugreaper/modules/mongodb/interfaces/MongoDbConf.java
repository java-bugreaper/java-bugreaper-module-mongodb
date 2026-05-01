package net.bugreaper.modules.mongodb.interfaces;

import net.bugreaper.modules.mongodb.MongoDb;

public interface MongoDbConf {

    /**
     * Configure global await for asserts with await
     *
     * @param awaitMs ms await
     * @return this instance for method chaining
     * @throws IllegalArgumentException on invalid setup
     */
    MongoDb setAwaitMs(int awaitMs);

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
