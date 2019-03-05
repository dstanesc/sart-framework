package org.sartframework.demo.cae.boot;

import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Assert;
import org.sartframework.demo.cae.client.LocalInputDeckQueryApi;
import org.sartframework.demo.cae.client.LocalSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.event.InputDeckCreatedEvent;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.LocalTransactionDriver;
import org.sartframework.driver.RemoteTransactionApi;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.kafka.channels.KafkaWriters;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorShowcase {

    final static Logger LOGGER = LoggerFactory.getLogger(SimulationClientBootstrap.class);
    
    static AtomicInteger inputDeckCounter = new AtomicInteger(1);

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
    
    final private KafkaWriters writeChannels;
    
    public MonitorShowcase(KafkaWriters writeChannels) {
        super();
        this.writeChannels = writeChannels;
    }
    
    public void testInputDeckPerformanceMonitor() throws Exception {

        SartKafkaConfiguration kafkaConfiguration = writeChannels.getSartKafkaConfiguration();
        
        TransactionDriver driver = new LocalTransactionDriver(writeChannels)
            .registerTransactionApi(new RemoteTransactionApi())
            .registerProjectionApi(new LocalInputDeckQueryApi(kafkaConfiguration))
            .registerCommandApi(new LocalSimulationApi()).init();

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
    
    private String nextInputDeckIdentity() {
        int i = inputDeckCounter.incrementAndGet();
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        return "ID_" + i + "_" + timeInMillis;
    }

}
