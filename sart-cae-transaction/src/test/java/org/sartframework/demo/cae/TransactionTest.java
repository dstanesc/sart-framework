package org.sartframework.demo.cae;

import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionStatus.Isolation;
import org.sartframework.command.transaction.TransactionStatus.Status;
import org.sartframework.demo.cae.client.RestInputDeckQueryApi;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.ForceValidationFailureCommand;
import org.sartframework.demo.cae.command.InputDeckAddResultCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.demo.cae.event.InputDeckCreatedEvent;
import org.sartframework.demo.cae.event.InputDeckFileUpdatedEvent;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RestConflictQueryApi;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.query.ConflictsByAggregateQuery;
import org.sartframework.result.ConflictResolvedResult;
import org.sartframework.session.SystemSnapshot;
import org.sartframework.transaction.TraceDetailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionTest.class);
    
    @Test
    public void testCommandSingle() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<TestStatus> completionLock = new CompletableFuture<>();

        CompletableFuture<InputDeckCreatedEvent> progressLock = new CompletableFuture<>();

        DomainTransaction domainTransaction = driver.createDomainTransaction();

        domainTransaction.onAbort(abortEvent -> {

            LOGGER.info("Transaction aborted");

            completionLock.complete(TestStatus.ABORTED);
        });

        domainTransaction.onCommit(commitEvent -> {

            LOGGER.info("Transaction commited");

            completionLock.complete(TestStatus.COMMITTED);
        });

        domainTransaction.onProgress(inputDeckCreated -> {

            LOGGER.info("InputDeckCreatedEvent event detected {}", inputDeckCreated.getInputDeckName());

            progressLock.complete(inputDeckCreated);

        }, InputDeckCreatedEvent.class);

        String i = nextInputDeckIdentity();

        domainTransaction.executeCommand(() -> {
            
            return new InputDeckCreateCommand(i, "input-deck-name-" + i, "input-deck-file-" + i);
        });

        TestStatus status = completionLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Transaction status is {}", status);

        if (TestStatus.COMMITTED != status) {
            fail("Unexpected status");
        } else {
            LOGGER.info("Success, committed transaction found");
        }

        InputDeckCreatedEvent atomicEvent = progressLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Progress event {}", atomicEvent);
    }

    @Test
    public void testCommandMultiple() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        DomainTransaction domainTransaction = driver.createDomainTransaction();

        CompletableFuture<Long> startLock = new CompletableFuture<>();

        CompletableFuture<Long> completionLock = new CompletableFuture<>();

        domainTransaction.onStart(start -> {

            Long processedTime = start.getCreationTime();

            LOGGER.info("Transaction started @ {}", processedTime);

            startLock.complete(processedTime);
        });

        domainTransaction.onCommit(commit -> {

            Long processedTime = commit.getCreationTime();

            LOGGER.info("Transaction commited @ {}", processedTime);

            completionLock.complete(processedTime);
        });

        String j = nextInputDeckIdentity();
        String k = nextResultIdentity();

        domainTransaction.executeCommandStream(() -> Stream.of(new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j),
            new InputDeckAddResultCommand(j, 0, k, "result-name-" + k, "result-file-" + k),
            new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn" + domainTransaction.getXid())));

        Long completed = completionLock.get(10, TimeUnit.SECONDS);

        Long started = startLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Transaction executed successfully Duation is {} ", (completed - started));

        Assert.assertTrue(completed > started);

    }

    @Test
    public void testConflictResolution1() throws Exception {

        CompletableFuture<ConflictResolvedEvent> conflictResolvedLock = new CompletableFuture<>();

        String j = nextInputDeckIdentity();

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        DomainTransaction x1 = driver.createDomainTransaction();

        x1.start();

        InputDeckCreateCommand c = new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j);

        InputDeckUpdateFileCommand u = new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn-xid-" + x1.getXid());

        x1.appendCommand(() -> c);

        x1.appendCommand(() -> u);

        x1.commit();

        DomainTransaction x2 = driver.createDomainTransaction();

        x2.onConflict(conflictResolved -> {

            LOGGER.info("Conflict resolved  {}", conflictResolved);

            conflictResolvedLock.complete(conflictResolved);
        });

        x2.start();

        InputDeckUpdateFileCommand u2 = new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn-second-xid-" + x2.getXid());

        x2.appendCommand(() -> u2);

        x2.commit();

        LOGGER.info("{} {} done", x1.getXid(), x2.getXid());

        ConflictResolvedEvent conflictResolved = conflictResolvedLock.get(10, TimeUnit.SECONDS);

        long winnerXid = conflictResolved.getWinnerXid();

        LOGGER.info("Winner xid is {} ", winnerXid);

        // verify LWW conflict resolution successful
        Assert.assertEquals(x2.getXid(), winnerXid);
    }

    @Test
    public void testConflictResolution2() throws Exception {

        CompletableFuture<ConflictResolvedEvent> conflictResolvedLock = new CompletableFuture<>();

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        DomainTransaction x1 = driver.createDomainTransaction();

        DomainTransaction x2 = driver.createDomainTransaction();

        x1.start();

        x2.start();

        x1.onConflict(conflictResolved -> {

            LOGGER.info("Conflict resolved  {}", conflictResolved);

            conflictResolvedLock.complete(conflictResolved);
        });

        String j = nextInputDeckIdentity();

        InputDeckCreateCommand c = new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j);

        InputDeckUpdateFileCommand u2 = new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn-second-xid-" + x2.getXid());

        InputDeckUpdateFileCommand u = new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn-xid-" + x1.getXid());

        x1.appendCommand(() -> c);

        x2.appendCommand(() -> u2);

        x1.appendCommand(() -> u);

        x1.commit();

        x2.commit();

        LOGGER.info("{} {} done", x1.getXid(), x2.getXid());

        ConflictResolvedEvent conflictResolved = conflictResolvedLock.get(10, TimeUnit.SECONDS);

        long winnerXid = conflictResolved.getWinnerXid();

        LOGGER.info("Winner xid is {} ", winnerXid);
        LOGGER.info("Conflict aggregate is {} ", conflictResolved.getAggregateKey());
        LOGGER.info("Conflict change key is {} ", conflictResolved.getChangeKey());

        // verify LWW conflict resolution successful
        Assert.assertEquals(x2.getXid(), winnerXid);

    }
    
    @SuppressWarnings("unused")
    @Test
    public void testAbortCreation() throws Exception {

        CompletableFuture<TransactionAbortedEvent> abortLock = new CompletableFuture<>();

        CompletableFuture<TransactionCompletedEvent> completedLock = new CompletableFuture<>();
        
        CompletableFuture<Boolean> queryCompletionLock = new CompletableFuture<>();

        TransactionDriver driver = new DefaultRestTransactionDriver()
            .registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi())
            .registerCommandApi(new RestSimulationApi())
            .registerDetailFactory(new TraceDetailFactory())
            .init();

        DomainTransaction x1 = driver.createDomainTransaction();

        String j = nextInputDeckIdentity();

        String x1InputDeckFile = "file-you-should-see" + x1.getXid();

        String inputDeckName = "input-deck-name-" + j;

        String inputDeckFile = "input-deck-file-" + j;

        x1.onAbort(abortEvent -> {

            abortLock.complete(abortEvent);

        });

        x1.onComplete(completedEvent -> {

            completedLock.complete(completedEvent);
        });

        x1.executeCommandStream(() -> {
            return Stream.of(new InputDeckCreateCommand(j, inputDeckName, inputDeckFile), new InputDeckUpdateFileCommand(j, 0, x1InputDeckFile),
                new ForceValidationFailureCommand(j, 0));
        });

        DomainTransaction x2 = x1.serialTransaction();

        AtomicInteger counter = new AtomicInteger(0);

        x2.executeQuery(false, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, 
         result -> counter.incrementAndGet(),
         error -> {}, 
         () -> {
            LOGGER.info("Completed");
            int resultCount = counter.get();
            Assert.assertEquals(0, resultCount);
            queryCompletionLock.complete(Boolean.TRUE);
        });

        TransactionAbortedEvent abortedEvent = abortLock.get(10, TimeUnit.SECONDS);

        TransactionCompletedEvent completedEvent = completedLock.get(10, TimeUnit.SECONDS);
        
        Boolean completed = queryCompletionLock.get(10, TimeUnit.SECONDS);
    }

    
    @SuppressWarnings("unused")
    @Test
    public void testAbortCreationMultipleUpdates() throws Exception {

        CompletableFuture<TransactionAbortedEvent> abortLock = new CompletableFuture<>();

        CompletableFuture<TransactionCompletedEvent> completedLock = new CompletableFuture<>();
        
        CompletableFuture<Boolean> queryCompletionLock = new CompletableFuture<>();

        TransactionDriver driver = new DefaultRestTransactionDriver()
            .registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi())
            .registerCommandApi(new RestSimulationApi())
            .registerDetailFactory(new TraceDetailFactory())
            .init();

        DomainTransaction x1 = driver.createDomainTransaction();

        String j = nextInputDeckIdentity();

        String x1InputDeckFile = "file-you-should-see" + x1.getXid();

        String inputDeckName = "input-deck-name-" + j;

        String inputDeckFile = "input-deck-file-" + j;

        x1.onAbort(abortEvent -> {

            abortLock.complete(abortEvent);
        });

        x1.onComplete(completedEvent -> {

            completedLock.complete(completedEvent);
            
            Status status = completedEvent.getStatus();
            
            Assert.assertEquals(Status.ABORTED, status);
        });

        Stream<? extends DomainCommand> create = Stream.of(new InputDeckCreateCommand(j, inputDeckName, inputDeckFile));
        
        Stream<? extends DomainCommand> update = Stream.generate(() -> {

            return buildInputDeckUpdateFileCommand(j, 0, nextFileIdentity());

        }).limit(100);
        
        
        Stream<? extends DomainCommand> fail =  Stream.of( new ForceValidationFailureCommand(j, 0));

        Stream<? extends DomainCommand> create_update_fail = Stream.concat(Stream.concat(create, update), fail);
        
        x1.executeCommandStream(() -> create_update_fail);

        DomainTransaction x2 = x1.serialTransaction();

        AtomicInteger counter = new AtomicInteger(0);

        x2.executeQuery(false, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, 
         result -> counter.incrementAndGet(),
         error -> {}, 
         () -> {
            LOGGER.info("Completed");
            int resultCount = counter.get();
            Assert.assertEquals(0, resultCount);
            queryCompletionLock.complete(Boolean.TRUE);
        });

        TransactionAbortedEvent abortedEvent = abortLock.get(10, TimeUnit.SECONDS);

        TransactionCompletedEvent completedEvent = completedLock.get(10, TimeUnit.SECONDS);
        
        Boolean completed = queryCompletionLock.get(10, TimeUnit.SECONDS);
    }

    
    @SuppressWarnings("unused")
    @Test
    public void testAbortCreationMultipleUpdates2() throws Exception {

        CompletableFuture<TransactionCompletedEvent> completedLock = new CompletableFuture<>();

        CompletableFuture<Boolean> queryCompletionLock = new CompletableFuture<>();

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        DomainTransaction x1 = driver.createDomainTransaction();

        String j = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + j;

        String inputDeckFile = "input-deck-file-" + j;

        x1.onComplete(completedEvent -> {

            completedLock.complete(completedEvent);

            Status status = completedEvent.getStatus();
            
            LOGGER.info("Long transaction completed with status {} ", status);

            Assert.assertEquals(Status.ABORTED, status);
        });

        AtomicInteger updateCounter = new AtomicInteger(0);

        //recursion on update
        x1.onProgress(fileUpdatedEvent -> {

            int updateCount = updateCounter.incrementAndGet();

            if (updateCount < 50) {
                
                long targetVersion = fileUpdatedEvent.getTargetAggregateVersion();

                InputDeckUpdateFileCommand inputDeckUpdateFileCommand = buildInputDeckUpdateFileCommand(j, targetVersion, nextFileIdentity());

                x1.appendCommand(() -> inputDeckUpdateFileCommand);
                
            } else {
                
                x1.commit();
            }

        }, InputDeckFileUpdatedEvent.class);

        x1.start();
        
        x1.appendCommandStream(() -> Stream.of(new InputDeckCreateCommand(j, inputDeckName, inputDeckFile),
            new InputDeckUpdateFileCommand(j, 0, inputDeckFile), new ForceValidationFailureCommand(j, 0)));

        DomainTransaction x2 = x1.serialTransaction();

        AtomicInteger queryCounter = new AtomicInteger(0);

        x2.executeQuery(false, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, result -> queryCounter.incrementAndGet(),
            error -> {
            }, () -> {
                LOGGER.info("Completed");
                int resultCount = queryCounter.get();
                Assert.assertEquals(0, resultCount);
                queryCompletionLock.complete(Boolean.TRUE);
            });

        TransactionCompletedEvent completedEvent = completedLock.get(10, TimeUnit.SECONDS);

        Boolean completed = queryCompletionLock.get(10, TimeUnit.SECONDS);
    }
    
    @SuppressWarnings("unused")
    @Test
    public void testAbort() throws Exception {

        CompletableFuture<TransactionAbortedEvent> abortLock = new CompletableFuture<>();

        CompletableFuture<InputDeckFileUpdatedEvent> fileCompensateLock = new CompletableFuture<>();

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        DomainTransaction x1 = driver.createDomainTransaction();

        String j = nextInputDeckIdentity();

        String x1InputDeckFile = "file-you-should-see" + x1.getXid();

        x1.executeCommandStream(() -> {
            return Stream.of(new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j),
                new InputDeckUpdateFileCommand(j, 0, x1InputDeckFile));
        });

        DomainTransaction x2 = driver.createDomainTransaction();

        x2.onAbort(abortEvent -> {

            abortLock.complete(abortEvent);

        }).onCompensate(fileUpdatedEvent -> {

            LOGGER.info("InputDeckFileUpdatedEvent event detected {}", fileUpdatedEvent.getInputDeckFile());

            fileCompensateLock.complete(fileUpdatedEvent);

        }, InputDeckFileUpdatedEvent.class);

        String k = nextResultIdentity();

        String x2InputDeckFile = "file-you-should-NOT-see" + x2.getXid();

        x2.executeCommandStream(() -> {
            return Stream.of(new InputDeckAddResultCommand(j, 0, k, "result-name-" + k, "result-file-" + k),
                new InputDeckUpdateFileCommand(j, 0, x2InputDeckFile), new ForceValidationFailureCommand(j, 0));
        });

        TransactionAbortedEvent abortedEvent = abortLock.get(10, TimeUnit.SECONDS);
        InputDeckFileUpdatedEvent compensateFileUpdatedEvent = fileCompensateLock.get(10, TimeUnit.SECONDS);
        String originalInputDeckFile = compensateFileUpdatedEvent.getOriginalInputDeckFile();
        String inputDeckFile = compensateFileUpdatedEvent.getInputDeckFile();

        Assert.assertEquals(x1InputDeckFile, inputDeckFile);
        Assert.assertEquals(x2InputDeckFile, originalInputDeckFile);

        Status x2Status = x2.getStatus();

        Status x1Status = x1.getStatus();

        Assert.assertEquals(Status.ABORTED, x2Status);

        Assert.assertEquals(Status.COMMITED, x1Status);
    }

    @Test
    public void testQueryConflictProjection() throws Exception {

        CompletableFuture<ConflictResolvedEvent> conflictNotificationLock = new CompletableFuture<>();

        CompletableFuture<ConflictResolvedResult> conflictQueryLock = new CompletableFuture<>();

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        DomainTransaction x1 = driver.createDomainTransaction();

        DomainTransaction x2 = driver.createDomainTransaction();

        x1.start();

        x2.start();

        x1.onConflict(conflictResolved -> {

            LOGGER.info("Conflict resolved  {}", conflictResolved);

            conflictNotificationLock.complete(conflictResolved);
        });

        String j = nextInputDeckIdentity();

        x1.onStartQuery(true, new ConflictsByAggregateQuery(j), ConflictResolvedResult.class, e -> {

            LOGGER.info("Found conflict for aggregate {} for change {}, xid={}, queryKey={} ", e.getAggregateKey(), e.getChangeKey(), e.getXid());

            conflictQueryLock.complete(e);

        }, e -> LOGGER.error("Got error on query", e), () -> LOGGER.info("Query completed {}"));

        InputDeckCreateCommand c = new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j);

        InputDeckUpdateFileCommand u2 = new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn-second-xid-" + x2.getXid());

        InputDeckUpdateFileCommand u = new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn-xid-" + x1.getXid());

        x1.appendCommand(() -> c);

        x2.appendCommand(() -> u2);

        x1.appendCommand(() -> u);

        x1.commit();

        x2.commit();

        LOGGER.info("{} {} done", x1.getXid(), x2.getXid());

        ConflictResolvedEvent conflictResolved = conflictNotificationLock.get(10, TimeUnit.SECONDS);

        long winnerXid = conflictResolved.getWinnerXid();

        LOGGER.info("Winner xid is {} ", winnerXid);
        LOGGER.info("Conflict aggregate is {} ", conflictResolved.getAggregateKey());
        LOGGER.info("Conflict change key is {} ", conflictResolved.getChangeKey());

        // verify LWW conflict resolution successful
        Assert.assertEquals(x2.getXid(), winnerXid);

        ConflictResolvedResult queryResult = conflictQueryLock.get(10, TimeUnit.SECONDS);

        Assert.assertEquals(queryResult.getWinnerXid(), conflictResolved.getWinnerXid());

        Assert.assertEquals(queryResult.getAggregateKey(), conflictResolved.getAggregateKey());

        Assert.assertEquals(queryResult.getChangeKey(), conflictResolved.getChangeKey());
    }

    @Test
    public void testQueryInputDeckProjectionSyntax1() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<SystemSnapshot> startLock = new CompletableFuture<>();

        String id = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + id;
        
        String inputDeckFile = "input-deck-file-" + id;

        DomainTransaction wx = driver.createDomainTransaction();

        wx.executeCommand(() -> new InputDeckCreateCommand(id, inputDeckName, inputDeckFile));

        driver.createDomainTransaction()

        .onStart(startedEvent -> {
            SystemSnapshot systemSnapshot = startedEvent.getSystemSnapshot();
            startLock.complete(systemSnapshot);
        })

        .start()

        .commit();

        SystemSnapshot systemSnapshot = startLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("RX  highest={} running={}", systemSnapshot.getHighestCommitted(), systemSnapshot.getRunning());

        Assert.assertTrue(systemSnapshot.getHighestCommitted() <= wx.getXid());
    }

    @SuppressWarnings("unused")
    @Test
    public void testQueryInputDeckProjectionSyntax2() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();
        CompletableFuture<TransactionCompletedEvent> completeLock = new CompletableFuture<>();

        String id = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + id;
        String inputDeckFile = "input-deck-file-" + id;

        DomainTransaction wx = driver.createDomainTransaction();

        wx.onComplete(c -> {

            DomainTransaction rx = driver.createDomainTransaction();

            rx.onStartQuery(true, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, result -> queryLock.complete(result));

            rx.start();

            rx.commit();

            completeLock.complete(c);
        });

        wx.executeCommand(() -> new InputDeckCreateCommand(id, inputDeckName, inputDeckFile));

        TransactionCompletedEvent transactionCompletedEvent = completeLock.get(10, TimeUnit.SECONDS);

        InputDeckQueryResult r = queryLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Found input deck result  xid={} name={} file={}", r.getXid(), r.getInputDeckName(), r.getInputDeckFile());

        Assert.assertEquals(r.getInputDeckName(), inputDeckName);
    }

    @Test
    public void testQueryInputDeckProjectionSyntax3() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        String id = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + id;
        String inputDeckFile = "input-deck-file-" + id;

        driver

            .createDomainTransaction()

            .executeCommand(() -> new InputDeckCreateCommand(id, inputDeckName, inputDeckFile))

            .serialTransaction()

            .onStartQuery(true, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, result -> queryLock.complete(result))

            .start()

            .commit();

        InputDeckQueryResult r = queryLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Found input deck result  xid={} name={} file={}", r.getXid(), r.getInputDeckName(), r.getInputDeckFile());

        Assert.assertEquals(r.getInputDeckName(), inputDeckName);
    }

    @Test
    public void testQueryInputDeckProjectionSyntax4() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        String id = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + id;
        String inputDeckFile = "input-deck-file-" + id;

        driver

            .createDomainTransaction()

            .executeCommand(() -> new InputDeckCreateCommand(id, inputDeckName, inputDeckFile))

            .serialTransaction()

            .executeQuery(true, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, result -> queryLock.complete(result));

        InputDeckQueryResult r = queryLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Found input deck result  xid={} name={} file={}", r.getXid(), r.getInputDeckName(), r.getInputDeckFile());

        Assert.assertEquals(r.getInputDeckName(), inputDeckName);
    }
    
    @Test
    public void testQueryInputDeckProjectionSyntax5() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        String id = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + id;
        String inputDeckFile = "input-deck-file-" + id;

        driver

            .createDomainTransaction(Isolation.READ_SNAPSHOT)

            .executeCommand(() -> new InputDeckCreateCommand(id, inputDeckName, inputDeckFile))

            .serialTransaction(Isolation.READ_SNAPSHOT)

            .executeQuery(true, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, result -> queryLock.complete(result));

        InputDeckQueryResult r = queryLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Found input deck result  xid={} name={} file={}", r.getXid(), r.getInputDeckName(), r.getInputDeckFile());

        Assert.assertEquals(r.getInputDeckName(), inputDeckName);
    }

    
    @Test
    public void testInputDeckPerformanceMonitor() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        String id = nextInputDeckIdentity();

        String inputDeckName = "input-deck-name-" + id;
        String inputDeckFile = "input-deck-file-" + id;

        InputDeckMonitor monitor = new InputDeckMonitor();

        driver.createDomainTransaction()

            .onStartQuery(true, new InputDeckByNameQuery(inputDeckName), InputDeckQueryResult.class, result -> {

                monitor.setEntityCreationTime(result.getEntityCreationTime());
                
                monitor.setResultCreationTime(result.getResultCreationTime());
                
                monitor.setQueryReturnTime(System.currentTimeMillis());

                queryLock.complete(result);
            })

            .onProgress(inputDeckCreated -> {
                
                monitor.setEventCreationTime(inputDeckCreated.getCreationTime());
                
            }, InputDeckCreatedEvent.class)

            .start()

            .appendCommand(() -> {

                InputDeckCreateCommand inputDeckCreateCommand = new InputDeckCreateCommand(id, inputDeckName, inputDeckFile);

                monitor.setCommandCreationTime(inputDeckCreateCommand.getCreationTime());

                return inputDeckCreateCommand;
            })

            .abort();

        InputDeckQueryResult r = queryLock.get(10, TimeUnit.SECONDS);

        Assert.assertEquals(r.getInputDeckName(), inputDeckName);

        long eventDispatchLatency = monitor.getEventCreationTime() - monitor.getCommandCreationTime();

        long projectionSynchLatency = monitor.getEntityCreationTime() - monitor.getEventCreationTime();
        
        long resultCreationLatency =  monitor.getResultCreationTime() - monitor.getEntityCreationTime();
        
        long resultTransferLatency =  monitor.getQueryReturnTime() - monitor.getResultCreationTime();
        
        long totalLatency = monitor.getQueryReturnTime() - monitor.getCommandCreationTime();

        LOGGER.info("Event latency = {} ms", eventDispatchLatency);

        LOGGER.info("Projection latency = {} ms", projectionSynchLatency);
        
        LOGGER.info("Result latency = {} ms", resultCreationLatency);
        
        LOGGER.info("Query latency = {} ms", resultTransferLatency);

        LOGGER.info("Total latency = {} ms", totalLatency);
    }
    
    
}
