package net.bugreaper.modules.mongodb;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import testcontainers.MongoContainerSetup;

import java.util.concurrent.CompletableFuture;

import static java.lang.Thread.sleep;

@SuppressWarnings("squid:S2699")
@Execution(ExecutionMode.CONCURRENT)
class MongoBasicParallelTest extends MongoContainerSetup {

    static MongoDb mg = getMongo();
    MongoDb mgConf = MongoDb.getInstance();

    private static final String COLLECTION = "users_parallel";
    private static final String COLLECTION2 = "users_parallel2";
    private static final String COLLECTION3 = "users_parallel3";


    @BeforeAll
    static void clean(){
        mg.cleanCollection(COLLECTION);
        mg.cleanCollection(COLLECTION3);
    }


    @Test
    void parallelObjTest(){

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> mg.seeCollectionIsNotEmpty(COLLECTION));
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> mg.seeRecordExistsInCollection(COLLECTION, """
                {
                    "name": "Anna"
                }"""));
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> pushWithSleep(mg, COLLECTION, "Anna", 500));

        CompletableFuture.allOf(future1, future2, future3).join();

    }

    @Test
    void parallel2Test(){


        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> mgConf.seeCollectionIsNotEmpty(COLLECTION2));
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> mgConf.seeRecordExistsInCollection(COLLECTION2, """
                {
                    "name": "Alex"
                }"""));
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> pushWithSleep(mgConf, COLLECTION2, "Alex",500));
        CompletableFuture<Void> future4 = CompletableFuture.runAsync(() -> pushWithSleep(mgConf, COLLECTION2, "John",300));

        CompletableFuture.allOf(future1, future2, future3, future4).join();

    }

    @Test
    void parallel3WaitForGrabTest(){

        mgConf.seeCollectionIsEmpty(COLLECTION3);

        CompletableFuture<Void> future1 = CompletableFuture.runAsync(() -> mgConf.seeCollectionIsNotEmpty(COLLECTION3));
        CompletableFuture<Void> future2 = CompletableFuture.runAsync(() -> mgConf.grabDocumentsFromCollection(COLLECTION3)
                .seeListAnyContainsJson("""
                {
                    "name": "John"
                }"""));
        CompletableFuture<Void> future3 = CompletableFuture.runAsync(() -> pushWithSleep(mgConf, COLLECTION3, "John", 500));

        CompletableFuture.allOf(future1, future2, future3).join();

    }


    @SuppressWarnings("squid:S2925")
    private void pushWithSleep(MongoDb obj, String collection, String name, int sleep){
        try {
            sleep(sleep);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        obj.insertIntoCollection(
                collection,
                String.format("""
                        {
                            "name": "%s"
                        }""", name)
        );
    }
    
}
