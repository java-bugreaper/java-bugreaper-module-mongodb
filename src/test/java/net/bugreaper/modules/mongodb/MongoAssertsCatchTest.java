package net.bugreaper.modules.mongodb;

import net.bugreaper.core.exceptions.FileReaderException;
import org.awaitility.core.ConditionTimeoutException;
import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import testcontainers.MongoContainerSetup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings({"java:S5976","java:S5778"})
@Isolated
class MongoAssertsCatchTest extends MongoContainerSetup {

    MongoDb mg = getMongo().setAwaitMs(400);
    private static final String COLLECTION = "test-collection";

    @BeforeEach
    void clean(){
        mg.cleanCollection(COLLECTION);
    }


    @Test
    void seeRecordsCountInCollectionExactlyFailedTest(){
        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            "name": 'Alex'
                        }"""
        );


        Throwable exception1 = assertThrows(AssertionError.class, () ->
                mg.withAwaitMs(210).seeDocumentsCountInCollectionExactly(COLLECTION, 2 ));

        assertEquals(
                String.format("Expected EXACTLY <2> documents in collection '%s', but got <1> within 210 milliseconds", COLLECTION),
                exception1.getMessage());

        Throwable exception2 = assertThrows(AssertionError.class, () ->
                mg.seeDocumentsCountInCollectionExactly(COLLECTION, 2 ));

        assertEquals(
                String.format("Expected EXACTLY <2> documents in collection '%s', but got <1> within 400 milliseconds", COLLECTION),
                exception2.getMessage());
    }

    @Test
    void seeCollectionIsNotEmptyFailedTest(){

        Throwable exception1 = assertThrows(AssertionError.class, () ->
                mg.withAwaitMs(200).seeCollectionIsNotEmpty(COLLECTION));

        assertEquals(
                String.format("Expected collection '%s' to be not empty, but got no documents within 200 milliseconds", COLLECTION),
                exception1.getMessage());

        Throwable exception2 = assertThrows(AssertionError.class, () ->
                mg.seeCollectionIsNotEmpty(COLLECTION));

        assertEquals(
                String.format("Expected collection '%s' to be not empty, but got no documents within 400 milliseconds", COLLECTION),
                exception2.getMessage());
    }


    @Test
    void seeCollectionIsEmptyFailedTest(){
        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            "name": 'Alex'
                        }"""
        );

        Throwable exception1 = assertThrows(AssertionError.class, () ->
                mg.withAwaitMs(200).seeCollectionIsEmpty(COLLECTION));


        assertEquals(
                String.format("Expected collection '%s' to be empty, but got <1> documents within 200 milliseconds", COLLECTION),
                exception1.getMessage());

        Throwable exception2 = assertThrows(AssertionError.class, () ->
                mg.seeCollectionIsEmpty(COLLECTION));


        assertEquals(
                String.format("Expected collection '%s' to be empty, but got <1> documents within 400 milliseconds", COLLECTION),
                exception2.getMessage());
    }

    @Test
    void seeCollectionCountGraterEmptyTest(){

        Throwable exception1 = assertThrows(AssertionError.class, () ->
                mg.withAwaitMs(200).seeDocumentsCountInCollectionIsGreaterThan(COLLECTION, 1));

        assertEquals(
                String.format("Expected the number of documents in collection '%s' to be GREATER than <1>, but got <0> within 200 milliseconds", COLLECTION),
                exception1.getMessage());

        Throwable exception2 = assertThrows(AssertionError.class, () ->
                mg.seeDocumentsCountInCollectionIsGreaterThan(COLLECTION, 1));

        assertEquals(
                String.format("Expected the number of documents in collection '%s' to be GREATER than <1>, but got <0> within 400 milliseconds", COLLECTION),
                exception2.getMessage());
    }

    @Test
    void seeCollectionCountGraterOneTest(){

        Throwable exception1 = assertThrows(AssertionError.class, () ->
                mg.withAwaitMs(200).seeDocumentsCountInCollectionIsGreaterThan(COLLECTION, 1));

        MatcherAssert.assertThat(
                exception1.getMessage(),
                StringContains.containsString(String.format("Expected the number of documents in collection '%s' to be GREATER than <1>, but got <0> within 200", COLLECTION)));

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            "name": 'Alex'
                        }"""
        );

        Throwable exception2 = assertThrows(AssertionError.class, () ->
                mg.seeDocumentsCountInCollectionIsGreaterThan(COLLECTION, 1));

        MatcherAssert.assertThat(
                exception2.getMessage(),
                StringContains.containsString(String.format("Expected the number of documents in collection '%s' to be GREATER than <1>, but got <1> within 400 milliseconds", COLLECTION)));
    }

    @Test
    void insertNotExistingTemplateTest(){

        Throwable exception = assertThrows(FileReaderException.class, () ->
                mg.insertTemplateIntoCollection(
                        "not.exist",
                        """
                                {
                                    "name": 'Alex'
                                }"""
                ));

        assertEquals(
                "File does not exist in resources: templates/mongodb/not.exist.json",
                exception.getMessage());
    }

    @Test
    void containsRecordTest(){

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 25
                        }"""
                );


        String expected = """
                        {
                            name: 'Alex2'
                        }""";

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeDocumentPartExistsInCollection(
                        COLLECTION,
                        expected
                ));

        assertEquals(String.format("""
                        No CONTAINS matching document found in collection '%s' within 400 milliseconds 
                        Checked documents: 1
                        
                        Expected:
                        {
                            name: 'Alex2'
                        }
                        
                        Differences:
                        [
                        
                        --- Actual #1---
                        {
                          "name": "Alex",
                          "age": 25
                        }
                        --- Differences ---
                         • name: expected [Alex2] but was [Alex]
                        
                        ]
                        """,COLLECTION),
                exception.getMessage());
    }

    @Test
    void containsRecordsTest(){

        MongoDb mgSize = getMongo().setMaxLastDocuments(1);

        mgSize.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Anna',
                            age: 33
                        }"""
        );

        mgSize.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 25
                        }"""
        );


        String expected = """
                        {
                            name: 'Alex2'
                        }""";

        Throwable exception = assertThrows(AssertionError.class, () ->
                mgSize.seeDocumentPartExistsInCollection(
                        COLLECTION,
                        expected
                ));

        assertEquals(String.format("""
                        No CONTAINS matching document found in collection '%s' within 2 seconds
                        Checked documents: 1
                        
                        Expected:
                        {
                            name: 'Alex2'
                        }
                        
                        Differences:
                        [
                        
                        --- Actual #1---
                        {
                          "name": "Alex",
                          "age": 25
                        }
                        --- Differences ---
                         • name: expected [Alex2] but was [Alex]
                        
                        ]
                        """,COLLECTION),
                exception.getMessage());
    }

    @Test
    void equalRecordTest(){

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            "name": "Alex",
                            "age": 25
                        }"""
        );

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            "names": "many"
                        }"""
        );

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeDocumentExistsInCollection(
                        COLLECTION,
                        """
                        {
                            "name": "Alex"
                        }
                        """
                ));

        assertEquals(String.format("""
                        No STRICT matching document found in collection '%s' within 400 milliseconds
                        Checked documents: 2
                        
                        Expected:
                        {
                            "name": "Alex"
                        }
                        
                        
                        Differences:
                        [
                        
                        --- Actual #1---
                        {
                          "names": "many"
                        }
                        --- Differences ---
                         • name: field missing
                         • names: unexpected field
                        
                        
                        -----------
                        
                        
                        --- Actual #2---
                        {
                          "name": "Alex",
                          "age": 25
                        }
                        --- Differences ---
                         • age: unexpected field
                        
                        ]
                        """,COLLECTION),
                exception.getMessage());
    }

    @Test
    void containsNullInRecordTest(){

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                            name: 'Alex',
                            age: 25
                        }"""
        );

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeDocumentExistsInCollection(
                        COLLECTION,
                        """
                        {
                            name: null
                        }
                        """
                ));

        assertEquals(String.format("""
                        No STRICT matching document found in collection '%s' within 400 milliseconds
                        Checked documents: 1
                                               
                        Expected:
                        {
                            name: null
                        }
                                               
                                               
                        Differences:
                        [
                                               
                        --- Actual #1---
                        {
                          "name": "Alex",
                          "age": 25
                        }
                        --- Differences ---
                         • name: expected null but was Alex
                         • age: unexpected field
                                               
                        ]
                        """,COLLECTION),
                exception.getMessage());
    }

    @Test
    void equalNoRecordTest(){

        Throwable exception1 = assertThrows(AssertionError.class, () ->
                mg.withAwaitMs(200).seeDocumentExistsInCollection(
                        COLLECTION,
                        """
                        {
                            "name": "John"
                        }
                        """
                ));

        assertEquals(String.format("Collection '%s' contains no documents within 200 milliseconds", COLLECTION),
                exception1.getMessage());

        Throwable exception2 = assertThrows(AssertionError.class, () ->
                mg.seeDocumentExistsInCollection(
                        COLLECTION,
                        """
                        {
                            "name": "Alex"
                        }
                        """
                ));

        assertEquals(String.format("Collection '%s' contains no documents within 400 milliseconds", COLLECTION),
                exception2.getMessage());

    }

    @Test
    void grabDocumentsFromCollectionNoRecordTest(){

        mg.withAwaitMs(200);

        Throwable exception = assertThrows(ConditionTimeoutException.class, () ->
                mg.grabDocumentsFromCollection(COLLECTION));

        assertEquals(String.format("No documents were received from collection '%s' within 200 milliseconds", COLLECTION),
                exception.getMessage());

    }

    @Test
    void equalRecordWithArraySizeFailedTest() {

        mg.cleanCollection(COLLECTION);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "26", "27"]
                            }
                        }"""
        );

        Throwable exception1 = assertThrows(AssertionError.class, () ->
                mg.withAwaitMs(210).seeDocumentExistsInCollection(
                        COLLECTION,
                        """
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "27"]
                            }
                        }"""
                ));

        MatcherAssert.assertThat(
                exception1.getMessage(),
                StringContains.containsString(String.format("No STRICT matching document found in collection '%s' within 210 milliseconds", COLLECTION)));


        Throwable exception2 = assertThrows(AssertionError.class, () ->
                mg.seeDocumentExistsInCollection(
                        COLLECTION,
                        """
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "27"]
                            }
                        }"""
                ));

        assertEquals(String.format("""
                        No STRICT matching document found in collection '%s' within 400 milliseconds
                        Checked documents: 1
                        
                        Expected:
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "27"]
                            }
                        }
                        
                        Differences:
                        [
                        
                        --- Actual #1---
                        {
                          "user": {
                            "name": "Alex",
                            "array": [
                              "25",
                              "26",
                              "27"
                            ]
                          }
                        }
                        --- Differences ---
                         • user.array: array size mismatch. expected 2 but was 3
                        
                        ]
                        """,COLLECTION),
                exception2.getMessage());
    }

    @Test
    void equalRecordWithArrayDataFailedTest() {

        mg.cleanCollection(COLLECTION);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "26", "27"]
                            }
                        }"""
        );

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeDocumentExistsInCollection(
                        COLLECTION,
                        """
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "27", "28"]
                            }
                        }"""
                ));

        assertEquals(String.format("""
                        No STRICT matching document found in collection '%s' within 400 milliseconds
                        Checked documents: 1
                        
                        Expected:
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "27", "28"]
                            }
                        }
                        
                        Differences:
                        [
                        
                        --- Actual #1---
                        {
                          "user": {
                            "name": "Alex",
                            "array": [
                              "25",
                              "26",
                              "27"
                            ]
                          }
                        }
                        --- Differences ---
                         • user.array[1]: expected [27] but was [26]
                         • user.array[2]: expected [28] but was [27]
                        
                        ]
                        """,COLLECTION),
                exception.getMessage());
    }

    @Test
    void equalRecordWithArrayDataFailed2Test() {

        mg.cleanCollection(COLLECTION);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                             "user": {
                                "name": "Alex",
                                "array": ["25", "27", "28"]
                            }
                        }"""
        );

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeDocumentPartExistsInCollection(
                        COLLECTION,
                        """
                        {
                             "user": {
                                "name": "Alex",
                                "array": ["25", "29"]
                            }
                        }"""
                ));

        assertEquals(String.format("""
                        No CONTAINS matching document found in collection '%s' within 400 milliseconds
                        Checked documents: 1
                        
                        Expected:
                        {
                             "user": {
                                "name": "Alex",
                                "array": ["25", "29"]
                            }
                        }
                        
                        Differences:
                        [
                        
                        --- Actual #1---
                        {
                          "user": {
                            "name": "Alex",
                            "array": [
                              "25",
                              "27",
                              "28"
                            ]
                          }
                        }
                        --- Differences ---
                         • user.array: array does not contain 29
                        
                        ]
                        """,COLLECTION),
                exception.getMessage());
    }

    @Test
    void equalRecordWithArrayOrderFailedTest() {

        mg.cleanCollection(COLLECTION);

        mg.insertIntoCollection(
                COLLECTION,
                """
                        {
                             user: {
                                name: 'Alex',
                                array: ["25", "26", "27"]
                            }
                        }"""
        );

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeDocumentExistsInCollection(
                        COLLECTION,
                        """
                        {
                             user: {
                                name: 'Alex',
                                array: ["27", "26", "25"]
                            }
                        }"""
                ));

        assertEquals(String.format("""
                        No STRICT matching document found in collection '%s' within 400 milliseconds
                        Checked documents: 1
                        
                        Expected:
                        {
                             user: {
                                name: 'Alex',
                                array: ["27", "26", "25"]
                            }
                        }
                        
                        Differences:
                        [
                        
                        --- Actual #1---
                        {
                          "user": {
                            "name": "Alex",
                            "array": [
                              "25",
                              "26",
                              "27"
                            ]
                          }
                        }
                        --- Differences ---
                         • user.array[0]: expected [27] but was [25]
                         • user.array[2]: expected [25] but was [27]
                        
                        ]
                        """,COLLECTION),
                exception.getMessage());
    }
}
