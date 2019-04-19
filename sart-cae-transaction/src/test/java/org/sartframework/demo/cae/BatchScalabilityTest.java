package org.sartframework.demo.cae;

import java.time.Duration;
import java.time.Instant;
import java.util.Calendar;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collector;
import java.util.stream.Collector.Characteristics;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.demo.cae.client.RestInputDeckQueryApi;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.BatchInputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.transaction.TraceDetailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BatchScalabilityTest {

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

    @Test
    public void testPerformance() throws Exception {

        createAndWait(200);
    }

    protected void createAndWait(int count) throws InterruptedException, ExecutionException, TimeoutException {

        TransactionDriver driver = new DefaultRestTransactionDriver()
            .registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi())
            .registerCommandApi(new RestSimulationApi())
            .registerDetailFactory(new TraceDetailFactory())
            .init();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        DomainTransaction wx = driver.createDomainTransaction();

        MutableValue<InputDeckCreateCommand> tracker = new MutableValue<>();

        Instant start = Instant.now();

        BatchInputDeckCreateCommand batch = Stream.generate(() -> {

            return buildCreateInputDeck(nextInputDeckIdentity());

        }).limit(count).peek(create -> {

            LOGGER.info("InputDeckName ={} ", create.getInputDeckName());

            tracker.setValue(create);

        }).collect(Collector.of(BatchInputDeckCreateCommand::new, BatchInputDeckCreateCommand::add, (left, right) -> {
            for (InputDeckCreateCommand element : right)
                left.add(element);
            return left;
        }, new Characteristics[0]));

        wx.executeCommandBatch(() -> batch);

        Instant afterCommandsReturned = Instant.now();

        DomainTransaction rx = driver.createDomainTransaction();

        String lastInputDeckName = tracker.getValue().getInputDeckName();

        LOGGER.info(" {} ->  chosen for query ", lastInputDeckName);

        rx.onStartQuery(true, new InputDeckByNameQuery(lastInputDeckName), InputDeckQueryResult.class,
            r -> queryLock.complete(r)).start().commit();

        InputDeckQueryResult r = queryLock.get(30, TimeUnit.SECONDS);

        Instant afterQueryReturned = Instant.now();

        Duration completeDuration = Duration.between(start, afterQueryReturned);

        Duration executionDuration = Duration.between(start, afterCommandsReturned);

        Duration queryWaitDuration = Duration.between(afterCommandsReturned, afterQueryReturned);

        LOGGER.info("Number of commands = {} ", count);
        LOGGER.info("Complete duration = {} s {} ms", completeDuration.getSeconds(), completeDuration.getNano() / 1000000);
        LOGGER.info("Commands duration = {} s {} ms", executionDuration.getSeconds(), executionDuration.getNano() / 1000000);
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
