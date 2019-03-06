package org.sartframework.demo.cae;

import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.demo.cae.client.RemoteInputDeckQueryApi;
import org.sartframework.demo.cae.client.RemoteSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.event.InputDeckCreatedEvent;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.RemoteTransactionApi;
import org.sartframework.driver.RemoteTransactionDriver;
import org.sartframework.driver.TransactionDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LatencyTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(LatencyTest.class);

    
    @Test
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

        long resultCreationLatency = monitor.getResultCreationTime() - monitor.getEntityCreationTime();

        long resultTransferLatency = monitor.getQueryReturnTime() - monitor.getResultCreationTime();

        long totalLatency = monitor.getQueryReturnTime() - monitor.getCommandCreationTime();

        LOGGER.info("Event latency = {} ms", eventDispatchLatency);

        LOGGER.info("Projection latency = {} ms", projectionSynchLatency);

        LOGGER.info("Result latency = {} ms", resultCreationLatency);

        LOGGER.info("Query latency = {} ms", resultTransferLatency);

        LOGGER.info("Total latency = {} ms", totalLatency);
    }
}
