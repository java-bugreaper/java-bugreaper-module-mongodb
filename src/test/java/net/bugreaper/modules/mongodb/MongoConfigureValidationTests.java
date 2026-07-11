package net.bugreaper.modules.mongodb;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import testcontainers.MongoContainerSetup;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;


@Isolated
class MongoConfigureValidationTests extends MongoContainerSetup {

    MongoDb test = getMongo();

    @Test
    void configMinusAwaitTest() {

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                test.setAwaitMs(-1));

        assertThat(
                exception.getMessage(),
                StringContains.containsString("awaitMs too small (can`t bee less 200ms)"));
    }

    @Test
    void configLessWithAwaitTest() {

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                test.withAwaitMs(100));

        assertThat(
                exception.getMessage(),
                StringContains.containsString("specificAwaitMs too small (can`t bee less 200ms)"));
    }

    @Test
    void setMaxLastRecords() {


        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                test.setMaxLastRecords(0));

        assertThat(
                exception.getMessage(),
                StringContains.containsString("maxLastRecords too small (can`t bee less 1)"));
    }

    @Test
    void configEmptyTemplatePathTest() {

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                test.setTemplatesDirectory(""));

        MatcherAssert.assertThat(
                "Error on config .setTemplatesDirectory empty",
                exception.getMessage(),
                StringContains.containsString("templatesPath can`t be empty or null"));
    }

    @Test
    void configNullTemplatePathTest() {

        Throwable exception = assertThrows(IllegalArgumentException.class, () ->
                test.setTemplatesDirectory(null));

        MatcherAssert.assertThat(
                "Error on config .setTemplatesDirectory empty",
                exception.getMessage(),
                StringContains.containsString("templatesPath can`t be empty or null"));
    }

}
