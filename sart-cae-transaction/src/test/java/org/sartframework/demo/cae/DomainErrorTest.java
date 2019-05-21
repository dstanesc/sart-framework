package org.sartframework.demo.cae;

import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.error.InputDeckInvalidFileError;
import org.sartframework.demo.cae.event.InputDeckCreatedEvent;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RestConflictQueryApi;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.transaction.TraceDetailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainErrorTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionTest.class);
    
    @Test
    public void testCommitWithErrorHandling() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<TestStatus> completionLock = new CompletableFuture<>();

        CompletableFuture<InputDeckCreatedEvent> progressLock = new CompletableFuture<>();
        
        CompletableFuture<InputDeckInvalidFileError> errorLock = new CompletableFuture<>();
        
        
        String i = nextInputDeckIdentity();

        DomainTransaction domainTransaction = driver.createDomainTransaction();
        
        domainTransaction
        
        /*
         * Define behavior on abort
         */
        
        .onAbort(abortEvent -> {

            LOGGER.info("Transaction aborted");

            completionLock.complete(TestStatus.ABORTED);
        })

        
        /*
         * Define behavior on commit
         */ 
        
        .onCommit(commitEvent -> {

            LOGGER.info("Transaction commited");

            completionLock.complete(TestStatus.COMMITTED);
        })

        
        /*
         * Commit transaction only when outcome confirmed
         */
        
        .onProgress(inputDeckCreated -> {
            
            domainTransaction.commit();

            LOGGER.info("InputDeckCreatedEvent event detected {}", inputDeckCreated.getInputDeckName());

            progressLock.complete(inputDeckCreated);

        }, InputDeckCreatedEvent.class)
        
       
        
        /*
         * Handle business error, such as re-publish the input deck with valid file 
         */
        
        .onDomainError (invalidFileError -> {
            
            String inputDeckFile = invalidFileError.getInputDeckFile();
            
            domainTransaction.appendCommand(() -> {

                return new InputDeckCreateCommand(i, "input-deck-name-" + i, "input-deck-valid-file-" + i);
            });
            
            errorLock.complete(invalidFileError);
            
        }, InputDeckInvalidFileError.class)
        
        
        /*
         * Start transaction
         */
        
        .start()
        
        /*
         * Publish invalid input deck
         */
        
        .appendCommand(() -> {

            return new InputDeckCreateCommand(i, "input-deck-name-" + i, "input-deck-invalid-file-" + i);
        });

        
        
        TestStatus status = completionLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Transaction status is {}", status);

        if (TestStatus.COMMITTED != status) {
            fail("Unexpected status " + status);
        } else {
            LOGGER.info("Success, committed transaction found");
        }

        InputDeckCreatedEvent domainEvent = progressLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Progress event {}", domainEvent);
        
        InputDeckInvalidFileError inputDeckInvalidFileError = errorLock.get(10, TimeUnit.SECONDS);
        
        LOGGER.info("InputDeckInvalidFile error recorded {}", inputDeckInvalidFileError.getInputDeckFile());
    }
    
    
    @Test
    public void testAbortWithErrorHandling() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<TestStatus> completionLock = new CompletableFuture<>();

        CompletableFuture<InputDeckCreatedEvent> progressLock = new CompletableFuture<>();
        
        CompletableFuture<InputDeckInvalidFileError> errorLock = new CompletableFuture<>();

        String i = nextInputDeckIdentity();
        

        
        DomainTransaction domainTransaction = driver.createDomainTransaction();
        
        
        /*
         * Define behavior on abort
         */
        
        domainTransaction.onAbort(abortEvent -> {

            LOGGER.info("Transaction aborted");

            completionLock.complete(TestStatus.ABORTED);
        });

        
        /*
         * Define behavior on commit
         */ 
        
        domainTransaction.onCommit(commitEvent -> {

            LOGGER.info("Transaction commited");

            completionLock.complete(TestStatus.COMMITTED);
        });

        
        /*
         * Abort transaction only when outcome confirmed
         */
        
        domainTransaction.onProgress(inputDeckCreated -> {
            
            domainTransaction.abort();

            LOGGER.info("InputDeckCreatedEvent event detected {}", inputDeckCreated.getInputDeckName());

            progressLock.complete(inputDeckCreated);

        }, InputDeckCreatedEvent.class);
        
       
        
        /*
         * Handle business error, such as re-publish the input deck with valid file 
         */
        
        domainTransaction.onDomainError (invalidFileError -> {
            
            String inputDeckFile = invalidFileError.getInputDeckFile();
            
            domainTransaction.appendCommand(() -> {

                return new InputDeckCreateCommand(i, "input-deck-name-" + i, "input-deck-valid-file-" + i);
            });
            
            errorLock.complete(invalidFileError);
            
        }, InputDeckInvalidFileError.class);
        
        
        /*
         * Start transaction
         */
        
        domainTransaction.start();
        
        
        /*
         * Publish invalid input deck
         */
        
        domainTransaction.appendCommand(() -> {

            return new InputDeckCreateCommand(i, "input-deck-name-" + i, "input-deck-invalid-file-" + i);
        });

        
        
        TestStatus status = completionLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Transaction status is {}", status);

        if (TestStatus.ABORTED != status) {
            fail("Unexpected status " + status);
        } else {
            LOGGER.info("Success, aborted transaction found");
        }

        InputDeckCreatedEvent domainEvent = progressLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Progress event {}", domainEvent);
        
        InputDeckInvalidFileError inputDeckInvalidFileError = errorLock.get(10, TimeUnit.SECONDS);
        
        LOGGER.info("InputDeckInvalidFile error recorded {}", inputDeckInvalidFileError.getInputDeckFile());
    }
}
