package org.sartframework.demo.cae;

import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.demo.cae.client.RemoteInputDeckQueryApi;
import org.sartframework.demo.cae.client.RemoteSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.demo.cae.event.InputDeckCreatedEvent;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.RemoteTransactionDriver;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RemoteConflictQueryApi;
import org.sartframework.driver.RemoteTransactionApi;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.query.ConflictsByAggregateQuery;
import org.sartframework.result.ConflictResolvedResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IncrementalTest {

    enum TestStatus {
        ABORTED, COMMITTED
    }

    final static Logger LOGGER = LoggerFactory.getLogger(IncrementalTest.class);

    static AtomicInteger inputDeckCounter = new AtomicInteger(1);

    static AtomicInteger resultCounter = new AtomicInteger(1);

    static AtomicInteger fileCounter = new AtomicInteger(1);

    static class InputDeckMonitor {

        Long commandCreationTime;

        Long eventCreationTime;

        Long entityCreationTime;
        
        Long queryReturnTime;
        
        Long resultCreationTime;

        public Long getCommandCreationTime() {
            return commandCreationTime;
        }

        public void setCommandCreationTime(Long commandCreationTime) {
            this.commandCreationTime = commandCreationTime;
        }

        public Long getEventCreationTime() {
            return eventCreationTime;
        }

        public void setEventCreationTime(Long eventCreationTime) {
            this.eventCreationTime = eventCreationTime;
        }

        public Long getEntityCreationTime() {
            return entityCreationTime;
        }

        public void setEntityCreationTime(Long entityCreationTime) {
            this.entityCreationTime = entityCreationTime;
        }

        public Long getResultCreationTime() {
            return resultCreationTime;
        }

        public void setResultCreationTime(Long resultCreationTime) {
            this.resultCreationTime = resultCreationTime;
        }

        public Long getQueryReturnTime() {
            return queryReturnTime;
        }

        public void setQueryReturnTime(Long queryReturnTime) {
            this.queryReturnTime = queryReturnTime;
        }

    }
    
   @Test
    @SuppressWarnings("unused")
    public void testQueryInputDeckProjectionSyntax2() throws Exception {

        TransactionDriver driver = new RemoteTransactionDriver().registerTransactionApi(new RemoteTransactionApi())
            .registerProjectionApi(new RemoteInputDeckQueryApi()).registerCommandApi(new RemoteSimulationApi()).init();

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

   //@Test
    public void testInputDeckPerformanceMonitor() throws Exception {

        TransactionDriver driver = new RemoteTransactionDriver().registerTransactionApi(new RemoteTransactionApi())
            .registerProjectionApi(new RemoteInputDeckQueryApi()).registerCommandApi(new RemoteSimulationApi()).init();

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

    
    
   
    protected Supplier<InputDeckUpdateFileCommand> buildInputDeckUpdateFileCommand(String id, long version, String inputDeckFile) {

        return () -> new InputDeckUpdateFileCommand(id, version, inputDeckFile);
    }

    private String nextInputDeckIdentity() {
        int i = inputDeckCounter.incrementAndGet();
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        return "ID_" + i + "_" + timeInMillis;
    }
}
