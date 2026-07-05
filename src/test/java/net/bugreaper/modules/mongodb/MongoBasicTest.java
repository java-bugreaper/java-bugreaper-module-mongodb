package net.bugreaper.modules.mongodb;

import ch.qos.logback.classic.Level;
import com.fasterxml.jackson.databind.JsonNode;
import net.bugreaper.core.utils.AllureAssert;
import net.bugreaper.core.utils.AllureResultLoader;
import net.bugreaper.core.utils.LogWatcher;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Isolated;

import testcontainers.MongoContainerSetup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("squid:S2699")
@Isolated
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class MongoBasicTest extends MongoContainerSetup {

    MongoDb mg = getMongo();

    private static final String COLLECTION = "users";

    private LogWatcher logWatcher;

    @BeforeAll
    static void cleanResults() {
        AllureResultLoader.cleanResultsDir();
    }

    @BeforeEach
    void setup() {
        logWatcher = new LogWatcher("bugreaper-module-mongodb", Level.DEBUG);
    }

    @AfterEach
    void teardown() {
        logWatcher.detach();
    }


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
        mg.seeRecordsCountInCollectionIsGreaterThan(COLLECTION, 1);
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

        mg.seeRecordsCountInCollectionExactly(COLLECTION, 4);

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

        assertEquals(
                String.format("[[INFO] Documents grabbed from collection <%s>: 2]", COLLECTION),
                logWatcher.getLoggedEvents(Level.INFO).toString());


    }

    @Test
    @Order(1)
    void testCheckRecordsLog() {

        MongoDb mgSize = getMongo().setMaxLastRecords(2);

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

        mgSize.seeRecordExistsInCollection(
                COLLECTION,"""
                        {"name": "Anna", "age": 25}"""
        );

        mgSize.seeRecordExistsInCollection(
                COLLECTION,"""
                         {"name": "Max", "age": 33}"""
        );


        logWatcher.clear();
        assertThrows(AssertionError.class, () ->
                mgSize.seeRecordPartExistsInCollection(
                        COLLECTION,"""
                         {"age": 26}"""
                ));

        //warning log only on assert failed!
        assertEquals(
                """
                [[WARN] Count of documents in collection <users> is <3>: more than maxLastRecords(2) in config
                only last documents will be taken into account (can be changed by .setMaxLastRecords(int) or config 'documents-max-count')]""",
                logWatcher.getLoggedEvents(Level.WARN).toString());

       var result = mgSize.grabDocumentsFromCollection(COLLECTION)
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
    }

    @Test
    @Order(2)
    void allureForListCheck() {
        JsonNode result = AllureResultLoader.loadByTestName("testCheckRecordsLog");

        AllureAssert.assertThat(result)


                .hasStep("(MongoDb)[ASSERT] Collection: <users> has record EQUAL to JSON")
                .hasAttachment("Expected:", """
                       {"name": "Anna", "age": 25}""")


                .hasStep("(MongoDb)[ASSERT] Collection: <users> has record CONTAINS JSON")
                .hasAttachment("Expected part:", """
                        {"age": 26}""")
                .hasAttachment("Differences:", """
                        [
                        
                        --- Actual #1---
                        {
                          "name": "Max",
                          "age": 33
                        }
                        --- Differences ---
                         • age: expected [26] but was [33]
                        
                        
                        -----------
                        
                        
                        --- Actual #2---
                        {
                          "name": "Anna",
                          "age": 25
                        }
                        --- Differences ---
                         • age: expected [26] but was [25]
                        
                        ]""")

                .hasStep("(MongoDb) Grab documents from collection: users")
                .hasAttachment("Documents(2) list:");
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
                            templatesPath=templates/mongodb/
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
                            awaitMs=800
                            maxLastRecords=15
                            templatesPath=templates/mongodb/
                        """, expectedUrl),
                mongo.getConfigSummary());
    }


    @Test
    void insertTemplateBasicTest(){

        mg.cleanCollection(COLLECTION);


        mg.insertTemplateIntoCollection(
                COLLECTION,
                """
                        {
                            "id": 2,
                            "type": null
                        }"""
        );

        assertEquals(
                "[[DEBUG] Looking for template file in resources: templates/mongodb/users.json]",
                logWatcher.getLoggedEvents(Level.DEBUG).toString());

        mg.seeRecordExistsInCollection(COLLECTION,
                """
                        {
                          "id": 2,
                          "type": null,
                          "params": {
                            "name": "Alex",
                            "age": 30,
                            "array": [
                              {
                                "product": "monitor",
                                "price": 99.99
                              }
                            ]
                          }
                        }""");


    }

    @Test
    void insertTemplateDeepArrayTest(){

        mg.cleanCollection(COLLECTION);

        mg.insertTemplateIntoCollection(
                COLLECTION,
                """
                        {
                            "id": 2,
                            "params": {
                                "name": "John",
                                "new": "test",
                                "array": [
                                    {
                                        "product": "CPU"
                                    }
                                ]
                            }
                        }"""
        );

        mg.seeRecordExistsInCollection(COLLECTION,
                """
                        {
                          "id": 2,
                          "type": "user",
                          "params": {
                            "name": "John",
                            "age": 30,
                            "new": "test",
                            "array": [
                              {
                                "product": "CPU",
                                "price": 99.99
                              }
                            ]
                          }
                        }""");
    }

    @Test
    void insertCustomTemplateBasicTest(){

        MongoDb mgCustom = getMongo().setTemplatesDirectory("templates/new-mongo/");
        String collection = "my_new_db.new.users";

        mgCustom.cleanCollection(collection);

        mgCustom.insertTemplateIntoCollection(
                collection,
                """
                        {
                            "id": 2
                        }"""
        );

        assertEquals(
                "[[DEBUG] Looking for template file in resources: templates/new-mongo/my_new_db.new.users.json]",
                logWatcher.getLoggedEvents(Level.DEBUG).toString());

        mgCustom.seeRecordExistsInCollection(collection,
                """
                        {
                          "id": 2,
                          "type": "new user",
                          "name": "Mark"
                        }""");

        assertEquals(String.format("""
                        MongoDb:
                            url=%s
                            default_database=test_db
                            awaitMs=2000
                            maxLastRecords=50
                            templatesPath=templates/new-mongo/
                        """, expectedUrl),
                mgCustom.getConfigSummary());
    }
}

