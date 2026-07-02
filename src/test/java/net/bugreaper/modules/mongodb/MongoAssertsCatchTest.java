package net.bugreaper.modules.mongodb;

import org.hamcrest.MatcherAssert;
import org.hamcrest.core.StringContains;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.parallel.Isolated;
import testcontainers.MongoSetup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SuppressWarnings("java:S5976")
@Isolated
class MongoAssertsCatchTest {

    MongoDb mg = MongoSetup.getInstance().getMongo().setAwaitMs(400);
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


        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeRecordsCountInCollectionExactly(COLLECTION, 2 ));

        MatcherAssert.assertThat(
                exception.getMessage(),
                StringContains.containsString(String.format("Count records from collection <%s> expected to be EXACTLY <2> but got <1> within 400 milliseconds", COLLECTION)));

    }

    @Test
    void seeCollectionIsNotEmptyFailedTest(){

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeCollectionIsNotEmpty(COLLECTION));

        MatcherAssert.assertThat(
                exception.getMessage(),
                StringContains.containsString(String.format("Collection <%s> expected to be not empty but got no records within 400 milliseconds", COLLECTION)));
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

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeCollectionIsEmpty(COLLECTION));

        MatcherAssert.assertThat(
                exception.getMessage(),
                StringContains.containsString(String.format("Collection <%s> expected to be empty but got <1> records within 400 milliseconds", COLLECTION)));
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
                mg.seeRecordPartExistsInCollection(
                        COLLECTION,
                        expected
                ));

        assertEquals(String.format("""
                        No CONTAINS matching found in collection <%s> 1 actual documents for:
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
            
                        ]""",COLLECTION),
                exception.getMessage());
    }

    @Test
    void containsRecordsTest(){

        MongoDb mgSize = MongoSetup.getInstance().getMongo().setMaxLastRecords(1);

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
                mgSize.seeRecordPartExistsInCollection(
                        COLLECTION,
                        expected
                ));

        assertEquals(String.format("""
                        No CONTAINS matching found in collection <%s> 1 actual documents for:
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
            
                        ]""",COLLECTION),
                exception.getMessage());
    }

    @Test
    void equalRecordTest(){

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

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeRecordExistsInCollection(
                        COLLECTION,
                        """
                        {
                            name: 'Alex'
                        }
                        """
                ));

        assertEquals(String.format("""
                        No STRICT matching found in collection <%s> 2 actual documents for:
                        {
                            name: 'Alex'
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
                        
                        ]""",COLLECTION),
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
                mg.seeRecordExistsInCollection(
                        COLLECTION,
                        """
                        {
                            name: null
                        }
                        """
                ));

        assertEquals(String.format("""
                        No STRICT matching found in collection <%s> 1 actual documents for:
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
                        
                        ]""",COLLECTION),
                exception.getMessage());
    }

    @Test
    void equalNoRecordTest(){
        
        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeRecordExistsInCollection(
                        COLLECTION,
                        """
                        {
                            "name": "Alex"
                        }
                        """
                ));

        assertEquals(String.format("Collection <%s> expected to be not empty but got no records within 400 milliseconds", COLLECTION),
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

        Throwable exception = assertThrows(AssertionError.class, () ->
                mg.seeRecordExistsInCollection(
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
                        No STRICT matching found in collection <%s> 1 actual documents for:
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
                        
                        ]""",COLLECTION),
                exception.getMessage());
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
                mg.seeRecordExistsInCollection(
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
                        No STRICT matching found in collection <%s> 1 actual documents for:
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
                        
                        ]""",COLLECTION),
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
                mg.seeRecordPartExistsInCollection(
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
                        No CONTAINS matching found in collection <%s> 1 actual documents for:
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
                        
                        ]""",COLLECTION),
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
                mg.seeRecordExistsInCollection(
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
                        No STRICT matching found in collection <%s> 1 actual documents for:
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
                        
                        ]""",COLLECTION),
                exception.getMessage());
    }
}
