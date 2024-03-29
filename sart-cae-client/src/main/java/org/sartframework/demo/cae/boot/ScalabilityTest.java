package org.sartframework.demo.cae.boot;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

import org.junit.Assert;
import org.sartframework.demo.cae.client.LocalTopicInputDeckQueryApi;
import org.sartframework.demo.cae.client.LocalTopicSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.DefaultTopicTransactionDriver;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.kafka.channels.KafkaWriters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ScalabilityTest {

    private static final String INPUT_DECK_FILE_PREFIX = "input-deck-file-";

    private static final String INPUT_DECK_NAME_PREFIX = "input-deck-name-";

    final static Logger LOGGER = LoggerFactory.getLogger(ScalabilityTest.class);

    enum TestStatus {
        ABORTED, COMMITTED
    }

    class MutableValue<T> {

        T value;

        public MutableValue() {
            super();
        }

        public T getValue() {
            return value;
        }

        public void setValue(T value) {
            this.value = value;
        }
    }

    static AtomicInteger inputDeckCounter = new AtomicInteger(1);

    static AtomicInteger resultCounter = new AtomicInteger(1);
    
    final KafkaWriters writeChannels;
    
    public ScalabilityTest(KafkaWriters writeChannels) {
        super();
        this.writeChannels = writeChannels;
    }

    public void testPerformance() throws Exception {

        createAndQuery(1000);
    }

    protected void createAndQuery(int count) throws InterruptedException, ExecutionException, TimeoutException {

        TransactionDriver driver = new DefaultTopicTransactionDriver(writeChannels)
            .registerTransactionApi(new RestTransactionApi())
            .registerProjectionApi(new LocalTopicInputDeckQueryApi(writeChannels.getSartKafkaConfiguration()))
            .registerCommandApi(new LocalTopicSimulationApi()).init();

        
//        TransactionDriver driver = new DefaultTransactionDriver().registerTransactionApi(new TransactionApi())
//            .registerProjectionApi(new InputDeckQueryApi()).registerCommandApi(new SimulationApi()).init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        DomainTransaction wx = driver.createDomainTransaction();

        MutableValue<InputDeckCreateCommand> tracker = new MutableValue<>();

        Instant start = Instant.now();

        wx.executeCommandStream(() -> Stream.generate(() -> {

            return buildCreateInputDeck(nextInputDeckIdentity());

        }).limit(count).peek(create -> {

            LOGGER.info("InputDeckName ={} ", create.getInputDeckName());

            tracker.setValue(create);
        }));

        Instant afterCommandsReturned = Instant.now();

        DomainTransaction rx = wx.serialTransaction();

        Instant afterSerialTransactionStarted = Instant.now();

        String lastInputDeckName = tracker.getValue().getInputDeckName();

        LOGGER.info(" {} ->  chosen for query ", lastInputDeckName);

        rx.executeQuery(false, new InputDeckByNameQuery(lastInputDeckName), InputDeckQueryResult.class, r -> queryLock.complete(r));

        InputDeckQueryResult r = queryLock.get(30, TimeUnit.SECONDS);

        Instant afterQueryReturned = Instant.now();

        Duration completeDuration = Duration.between(start, afterQueryReturned);

        Duration executionDuration = Duration.between(start, afterCommandsReturned);

        Duration launchingSerialTransaction = Duration.between(afterCommandsReturned, afterSerialTransactionStarted);

        Duration queryWaitDuration = Duration.between(afterSerialTransactionStarted, afterQueryReturned);

        LOGGER.info("Number of commands = {} ", count);
        LOGGER.info("Complete duration = {} s {} ms", completeDuration.getSeconds(), completeDuration.getNano() / 1000000);
        LOGGER.info("Commands duration = {} s {} ms", executionDuration.getSeconds(), executionDuration.getNano() / 1000000);
        LOGGER.info("Launch serial transaction wait duration = {} s {} ms", launchingSerialTransaction.getSeconds(),
            launchingSerialTransaction.getNano() / 1000000);
        LOGGER.info("Query wait duration = {} s {} ms", queryWaitDuration.getSeconds(), queryWaitDuration.getNano() / 1000000);

        Assert.assertEquals(r.getInputDeckName(), lastInputDeckName);
    }

    protected InputDeckCreateCommand buildCreateInputDeck(String id) {

        String inputDeckName = buildInputDeckName(id);

        String inputDeckFile = buildInputDeckFile(id);

        return new InputDeckCreateCommand(id, inputDeckName, inputDeckFile);
    }

    protected String buildInputDeckFile(String id) {
        return INPUT_DECK_FILE_PREFIX + id;
    }

    protected String buildInputDeckName(String id) {
        return INPUT_DECK_NAME_PREFIX + id;
    }

    private String nextInputDeckIdentity() {
        int i = inputDeckCounter.incrementAndGet();
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        return "ID_" + i + "_" + timeInMillis;
    }
}
