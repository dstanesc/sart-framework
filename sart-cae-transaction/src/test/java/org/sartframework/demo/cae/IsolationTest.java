package org.sartframework.demo.cae;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.junit.Test;
import org.sartframework.command.transaction.TransactionStatus.Isolation;
import org.sartframework.demo.cae.client.RestInputDeckQueryApi;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.query.InputDeckByIdQuery;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.transaction.TraceDetailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.junit.Assert;

public class IsolationTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(IsolationTest.class);
    
    @Test
    public void testReadSnapshotIsolationNotFound() {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        String j = nextInputDeckIdentity();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        // write transaction

        DomainTransaction wx = driver.createDomainTransaction();

        wx.onStart(startedEvent -> {

            // trigger later a read transaction unable to see writes from
            // predecessor as uncommitted yet
            DomainTransaction rx = driver.createDomainTransaction(Isolation.READ_SNAPSHOT);

            rx.onStartQuery(true, new InputDeckByIdQuery(j), InputDeckQueryResult.class, inputDeck -> {
                // should not get notified as not visible
                queryLock.complete(inputDeck);
            });

            rx.onComplete(completedEvent -> {

                // commit write transaction only after completion of the read
                // transaction
                wx.commit();
            });

            rx.start().commit();
        })

        .start()

        .appendCommand(() -> new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j));

        // Expect timeout as created inputDeck not visible because of the
        // isolation level

        try {

            queryLock.get(10, TimeUnit.SECONDS);

            throw new RuntimeException("Should fail before w/ TimeoutException");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            // expected
        }
    }

    @Test
    public void testReadCommitedIsolationNotFound() {
        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        String j = nextInputDeckIdentity();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        // write transaction

        DomainTransaction wx = driver.createDomainTransaction();

        wx.onStart(startedEvent -> {

            // trigger later a read transaction unable to see writes from
            // predecessor as uncommitted yet
            DomainTransaction rx = driver.createDomainTransaction(Isolation.READ_COMMITTED);

            rx.onStartQuery(true, new InputDeckByIdQuery(j), InputDeckQueryResult.class, inputDeck -> {
                // should not get notified as not visible
                queryLock.complete(inputDeck);
            });

            rx.onComplete(completedEvent -> {

                // commit write transaction only after completion of the read
                // transaction
                wx.commit();
            });

            rx.start().commit();
        })

        .start()

        .appendCommand(() -> new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j));

        // Expect timeout as created inputDeck not visible because of the
        // isolation level

        try {

            queryLock.get(10, TimeUnit.SECONDS);

            throw new RuntimeException("Should fail before w/ TimeoutException");

        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            // expected
        }
    }

    @Test
    public void testReadUncommitedIsolationFound() {
        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        String j = nextInputDeckIdentity();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        // write transaction

        DomainTransaction wx = driver.createDomainTransaction();

        wx.onStart(startedEvent -> {

            // trigger later a read transaction unable to see writes from
            // predecessor as uncommitted yet
            DomainTransaction rx = driver.createDomainTransaction(Isolation.READ_UNCOMMITTED);

            rx.onStartQuery(true, new InputDeckByIdQuery(j), InputDeckQueryResult.class, inputDeck -> {
               
                queryLock.complete(inputDeck);
            });

            rx.onComplete(completedEvent -> {

                // commit write transaction only after completion of the read
                // transaction
                wx.commit();
            });

            rx.start().commit();
        })

        .start()

        .appendCommand(() -> new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j));

        // Expect visible because using READ_UNCOMMITTED isolation level

        try {

            InputDeckQueryResult inputDeck = queryLock.get(10, TimeUnit.SECONDS);

            Assert.assertEquals(j, inputDeck.getInputDeckId());
            
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
    
    @Test
    public void testReadSnapshotIsolationFound() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        String id = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + id;
        String inputDeckFile = "input-deck-file-" + id;

        driver

            .createDomainTransaction()

            .executeCommand(() -> new InputDeckCreateCommand(id, inputDeckName, inputDeckFile))

            .serialTransaction(Isolation.READ_SNAPSHOT)

            .executeQuery(true, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, result -> queryLock.complete(result));

        InputDeckQueryResult r = queryLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Found input deck result  xid={} name={} file={}", r.getXid(), r.getInputDeckName(), r.getInputDeckFile());

        Assert.assertEquals(r.getInputDeckName(), inputDeckName);
    }
    
    @Test
    public void testReadCommittedIsolationFound() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        String id = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + id;
        String inputDeckFile = "input-deck-file-" + id;

        driver

            .createDomainTransaction()

            .executeCommand(() -> new InputDeckCreateCommand(id, inputDeckName, inputDeckFile))

            .serialTransaction(Isolation.READ_COMMITTED)

            .executeQuery(true, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, result -> queryLock.complete(result));

        InputDeckQueryResult r = queryLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Found input deck result  xid={} name={} file={}", r.getXid(), r.getInputDeckName(), r.getInputDeckFile());

        Assert.assertEquals(r.getInputDeckName(), inputDeckName);
    }
}
