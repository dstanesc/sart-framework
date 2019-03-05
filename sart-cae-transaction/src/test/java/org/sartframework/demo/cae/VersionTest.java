package org.sartframework.demo.cae;

import java.util.Calendar;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.sartframework.demo.cae.client.RemoteInputDeckQueryApi;
import org.sartframework.demo.cae.client.RemoteSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.demo.cae.query.InputDeckByIdQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.RemoteTransactionDriver;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RemoteTransactionApi;
import org.sartframework.driver.TransactionDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionTest {

    final static Logger LOGGER = LoggerFactory.getLogger(VersionTest.class);

    static AtomicInteger inputDeckCounter = new AtomicInteger(1);

    public VersionTest() {
    }

    // @Test
    public void testUseCaseAssumesVersions() throws Exception {

        TransactionDriver driver = new RemoteTransactionDriver()
            .registerTransactionApi(new RemoteTransactionApi())
            .registerProjectionApi(new RemoteInputDeckQueryApi())
            .registerCommandApi(new RemoteSimulationApi())
            .init();

        String j = nextInputDeckIdentity();

        DomainTransaction wx1 = driver.createDomainTransaction();

        wx1.executeCommand(() -> new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j));

        DomainTransaction wx2 = driver.createDomainTransaction();

        wx2.executeCommand(() -> new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn-xid-" + wx2.getXid()));

        DomainTransaction wx3 = driver.createDomainTransaction();

        wx3.executeCommand(() -> new InputDeckUpdateFileCommand(j, 1, "input-deck-file-updated-txn-second-xid-" + wx3.getXid()));

    }

    @Test
    public void testMultipleVersions() throws Exception {

        TransactionDriver driver = new RemoteTransactionDriver()
            .registerTransactionApi(new RemoteTransactionApi())
            .registerProjectionApi(new RemoteInputDeckQueryApi())
            .registerCommandApi(new RemoteSimulationApi())
            .init();

        String inputDeckId = nextInputDeckIdentity();

        DomainTransaction rx = driver.createDomainTransaction();

        CompletableFuture<InputDeckQueryResult> queryLock = new CompletableFuture<>();

        rx.onStartQuery(true, new InputDeckByIdQuery(inputDeckId), InputDeckQueryResult.class, inputDeck -> {

            long inputDeckVersion = inputDeck.getInputDeckVersion();

            LOGGER.info("InputDeckByIdQuery returned version {}, value {}", inputDeckVersion, inputDeck);

            if (inputDeckVersion == 0) {

                DomainTransaction wx2 = driver.createDomainTransaction();

                LOGGER.info("Executing input deck file update first xid={}", wx2.getXid());

                wx2.executeCommand(() -> new InputDeckUpdateFileCommand(inputDeckId, inputDeckVersion, "input-deck-file-updated-txn-xid-" + wx2.getXid()));

            } else if (inputDeckVersion == 1) {

                DomainTransaction wx3 = driver.createDomainTransaction();

                LOGGER.info("Executing input deck file update second xid={}", wx3.getXid());

                wx3.executeCommand(() -> new InputDeckUpdateFileCommand(inputDeckId, 1, "input-deck-file-updated-second-txn-xid-" + wx3.getXid()));

            } else if (inputDeckVersion == 2) {

                LOGGER.info("InputDeck version {} received, completing lock", inputDeckVersion);

                queryLock.complete(inputDeck);
                
            } else throw new IllegalStateException("unexpected verison " + inputDeckVersion);
        });
        
        DomainTransaction wx1 = driver.createDomainTransaction();

        wx1.executeCommand(() -> new InputDeckCreateCommand(inputDeckId, "input-deck-name-" + inputDeckId, "input-deck-file-" + inputDeckId));

        InputDeckQueryResult finalInputDeck = queryLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Found input deck result xid={} name={} file={}", finalInputDeck.getXid(), finalInputDeck.getInputDeckName(),
            finalInputDeck.getInputDeckFile());

    }

    private String nextInputDeckIdentity() {
        int i = inputDeckCounter.incrementAndGet();
        long timeInMillis = Calendar.getInstance().getTimeInMillis();
        return "ID_" + i + "_" + timeInMillis;
    }

}
