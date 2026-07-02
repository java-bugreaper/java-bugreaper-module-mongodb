package net.bugreaper.modules.mongodb;

import net.bugreaper.modules.mongodb.exceptions.MongoDBHelperException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import testcontainers.MongoSetup;

import static org.junit.jupiter.api.Assertions.assertThrows;


@Isolated
class MongoNegativeTest {

    MongoDb mg = MongoSetup.getInstance().getMongo();

    @BeforeEach
    void clean(){
        mg.cleanCollection("users");
    }


    @Test
    void emptyCollectionTest(){

        Throwable exception = assertThrows(MongoDBHelperException.class, () ->
                mg.insertIntoCollection(
                        "",
                        """
                                {
                                    name: 'Alex'
                                }"""
                ));

        MatcherAssert.assertThat(
                exception.getMessage(),
                StringContains.containsString("Failed to insert document: Collection can't be null or empty"));
    }

    @Test
    void nullCollectionTest(){

        Throwable exception = assertThrows(MongoDBHelperException.class, () ->
                mg.insertIntoCollection(
                        null,
                        """
                                {
                                    name: 'Alex'
                                }"""
                ));

        MatcherAssert.assertThat(
                exception.getMessage(),
                StringContains.containsString("Failed to insert document: Collection can't be null or empty"));
    }

    @Test
    void arrayInsertTest(){

        Throwable exception = assertThrows(MongoDBHelperException.class, () ->
                mg.insertIntoCollection(
                        "test",
                        """
                                [{
                                    name: 'Alex'
                                }]"""
                ));

        MatcherAssert.assertThat(
                exception.getMessage(),
                StringContains.containsString("Failed to insert document"));
    }

}
