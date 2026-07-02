package net.bugreaper.modules.mongodb;



import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import testcontainers.MongoSetup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Isolated
class MongoConfigureValidationTests {

    @Test
    void configMinusAwaitTest() {

        MongoDb test = MongoSetup.getInstance().getMongo();

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                test.setAwaitMs(-1));

        assertThat(
                exception.getMessage(),
                StringContains.containsString("awaitMs too small (can`t bee less 200ms)"));
    }

    @Test
    void setMaxLastRecords() {

        MongoDb test = MongoSetup.getInstance().getMongo();

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                test.setMaxLastRecords(0));

        assertThat(
                exception.getMessage(),
                StringContains.containsString("maxLastRecords too small (can`t bee less 1)"));
    }

}
