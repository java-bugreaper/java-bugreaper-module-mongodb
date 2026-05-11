package net.bugreaper.modules.mongodb;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;
import helpers.MemoryAppender;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.LoggerFactory;
import testcontainers.MongoSetup;

import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static testcontainers.MongoSetup.expectedUrl;

@SuppressWarnings("squid:S2699")
class MongoBasicTest {

    MongoDb mg = MongoSetup.getInstance().getMongo();

    private static final String COLLECTION = "users";

    private static final MemoryAppender memoryAppender = new MemoryAppender();
    private static final  String LOGGER_NAME = "bugreaper-module-mongodb";
    
    @BeforeEach
    void clean(){
        mg.cleanCollection(COLLECTION);
    }
    
    
    @Test
    void emptyNotEmptyTest(){

        mg.cleanCollection(COLLECTION);

        mg.seeCollectionIsEmpty(COLLECTION);
        mg.seeRecordsCountInCollectionExactly(COLLECTION, 0);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex'
                        }"""
        );

        mg.seeCollectionIsNotEmpty(COLLECTION);
        mg.seeRecordsCountInCollectionExactly(COLLECTION, 1);
        assertEquals(1, mg.getRecordsCountInCollection(COLLECTION));
    }

    @Test
    void insertAndAssertsRecordTest(){

        mg.cleanCollection(COLLECTION);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 25
                        }"""
                );

        mg.seeRecordPartExistsInCollection(
                COLLECTION,
                """
                {
                    name: 'Alex'
                }
                """
        );

        mg.seeRecordExistsInCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 25
                        }"""
        );

        mg.seeRecordsCountInCollectionExactly(COLLECTION, 1);

    }


    @Test
    void insertAndAssertsIncludeRecordTest() {

        mg.cleanCollection(COLLECTION);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                             user: {
                                "name": "Alex",
                                "age": 25
                            }
                        }"""
        );

        mg.seeRecordPartExistsInCollection(
                COLLECTION,
                """
                        {
                              user: {
                                 name: 'Alex'
                             }
                         }"""
        );

        mg.seeRecordExistsInCollection(
                COLLECTION,
                """
                        {
                             user: {
                                "name": 'Alex',
                                "age": 25
                            }
                        }"""
        );

        mg.seeRecordsCountInCollectionExactly(COLLECTION, 1);

    }


    @Test
    void insertAndAssertsRecordArraysTest() {

        mg.cleanCollection(COLLECTION);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "26", "27"]
                            }
                        }""");

        mg.seeRecordsCountInCollectionExactly(COLLECTION, 1);

        mg.seeRecordPartExistsInCollection(
                COLLECTION,
                """
                        {
                              user: {
                                 name: 'Alex',
                                 array: ["27", "26"]
                             }
                         }"""
        );

        mg.seeRecordExistsInCollection(
                COLLECTION,
                """
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "26", "27"]
                            }
                        }"""
        );

    }

    @Test
    void insertAndAssertsManyDatabasesTest(){

        mg.cleanCollection(COLLECTION);
        mg.cleanCollection("test2.users2");

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 25
                        }"""
        );
        mg.insertIntoCollection(
                "test2.users2",
                """
                        {
                            name: 'Alex2',
                            age: 225
                        }"""
        );
        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 25
                        }"""
        );

        mg.seeRecordsCountInCollectionExactly(COLLECTION, 2);
        mg.seeRecordsCountInCollectionExactly("test2.users2", 1);
    }


    @Test
    void insertAndAssertsCollectionWithDotTest(){

        mg.cleanCollection("test_db.my.collect");

        mg.insertIntoCollection(
                "test_db.my.collect",
                """
                        {
                            "name": "Alex2",
                        }"""
        );

        mg.seeRecordsCountInCollectionExactly("test_db.my.collect", 1);
    }

    @Test
    void equalRecordsTest(){


        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 26
                        }"""
        );

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            names: 'Alex',
                        }"""
        );

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 25
                        }"""
        );

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            names: 'many'
                        }"""
        );

        mg.seeRecordExistsInCollection(
                COLLECTION,
                """
                {
                          name: 'Alex',
                            age: 25
                }
                """
        );
    }

    @Test
    void grabRecordsAndAssertTest(){

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {name: 'Alex', "age": 26}"""
        );

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {"name": "Anna", "age": 25}"""
        );

        mg.grabDocumentsFromCollection(COLLECTION)
                .seeListHasExactlyCount(2)
                .seeListAnyContainsJson( """
                        {"name": "Alex", "age": 26}""")
                .seeListAnyContainsExtendedJson( """
                        {"name:like": "An", "age:<": 26}""");

    }

    @Test
    void testCountConsumedMessagesLog() {

        Logger logger = (Logger) LoggerFactory.getLogger(LOGGER_NAME);
        logger.setLevel(Level.WARN);
        logger.addAppender(memoryAppender);

        memoryAppender.setContext((LoggerContext) LoggerFactory.getILoggerFactory());
        memoryAppender.start();


        MongoDb mgSize = MongoSetup.getInstance().getMongo().setMaxLastRecords(2);

        mgSize.insertIntoCollection(
                COLLECTION,"""
                        {name: 'Alex', "age": 26}"""
        );
        mgSize.insertIntoCollection(
                COLLECTION,"""
                        {"name": "Anna", "age": 25}"""
        );
        mgSize.insertIntoCollection(
                COLLECTION,"""
                        {"name": "Max", "age": 33}"""
        );


        mgSize.seeRecordsCountInCollectionExactly(COLLECTION, 3);

        mgSize.seeRecordPartExistsInCollection(
                COLLECTION,"""
                         {"age": 25}"""
        );

        mgSize.seeRecordPartExistsInCollection(
                COLLECTION,"""
                         {"age": 33}"""
        );

        assertThrows(AssertionError.class, () ->
                mgSize.seeRecordPartExistsInCollection(
                        COLLECTION,"""
                         {"age": 26}"""
                ));

        String expectedLog = String.format("Count of documents in collection <%s>: more than maxLastRecords(%d) in config", COLLECTION, 2);

        assertThat(
                "Check WARN",
                memoryAppender.getLoggedEvents().toString(),
                StringContains.containsString(expectedLog));

        memoryAppender.reset();

       var result =  mgSize.grabDocumentsFromCollection(COLLECTION)
                .seeListHasExactlyCount(2)
                .seeListAnyContainsExtendedJson("""
                        {"age:>=": 30}""")
                .seeListAnyContainsExtendedJson(
                        """
                        {"age:=": 25}""");

        assertThrows(AssertionError.class, () ->
                result.seeListAnyContainsJson("""
                          {"age": 26}"""
                ));

        assertThat(
                "Check WARN",
                memoryAppender.getLoggedEvents().toString(),
                StringContains.containsString(expectedLog));
    }

    @Test
    void configCheckBasicTest(){

        MongoDb mongo = MongoDb.getInstance();

        mongo.cleanCollection(COLLECTION);

        mongo.seeCollectionIsEmpty(COLLECTION);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex'
                        }"""
        );

        mg.seeCollectionIsNotEmpty(COLLECTION);
    }

    @Test
    void checkSummaryTest(){


        assertEquals(String.format("""
                        MongoDb:
                            url=%s
                            default_database=test_db
                            awaitMs=2000
                            maxLastRecords=50
                        """, expectedUrl),
                mg.getConfigSummary());
    }

    @Test
    void configCheckSummaryTest(){

        MongoDb mongo = MongoDb.getInstance();

        assertEquals(String.format("""
                        MongoDb:
                            url=%s
                            default_database=test_db
                            awaitMs=420
                            maxLastRecords=15
                        """, expectedUrl),
                mongo.getConfigSummary());
    }

    @Test
    void parallelTest(){
        String collection = COLLECTION;

        mg.seeCollectionIsEmpty(collection);

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> mg.seeCollectionIsNotEmpty(collection));
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> mg.seeRecordsCountInCollectionExactly(collection, 1));
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> pushWithSleep(collection));

        CompletableFuture.allOf(future1, future2, future3).join();

    }

    @SuppressWarnings("squid:S2925")
    private void pushWithSleep(String collection){
        try {
            sleep(700);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        mg.insertIntoCollection(
                collection,
                """
                        {
                            name: 'Alex'
                        }"""
        );
    }
    
}
