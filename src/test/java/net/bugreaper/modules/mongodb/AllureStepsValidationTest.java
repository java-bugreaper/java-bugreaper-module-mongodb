package net.bugreaper.modules.mongodb;

import org.junit.jupiter.api.Test;

import static net.bugreaper.core.utils.AllureStepsValidator.validateAllSteps;

class AllureStepsValidationTest {

    @Test
    void testStepsMongoDb() {
        validateAllSteps("net.bugreaper.modules.mongodb.MongoDb");
    }

}
