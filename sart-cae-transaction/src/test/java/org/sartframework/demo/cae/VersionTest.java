package org.sartframework.demo.cae;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.sartframework.command.transaction.TransactionStatus.Isolation;
import org.sartframework.demo.cae.client.RestInputDeckQueryApi;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.demo.cae.query.InputDeckByIdQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.TransactionDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class VersionTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(VersionTest.class);

    @Test
    public void testMultipleVersions() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver()
            .registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestInputDeckQueryApi())
            .registerCommandApi(new RestSimulationApi())
            .init();

        String inputDeckId = nextInputDeckIdentity();

        DomainTransaction rx = driver.createDomainTransaction(Isolation.READ_UNCOMMITTED);

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
        
        rx.start().commit();
        
        DomainTransaction wx1 = driver.createDomainTransaction();

        wx1.executeCommand(() -> new InputDeckCreateCommand(inputDeckId, "input-deck-name-" + inputDeckId, "input-deck-file-" + inputDeckId));

        InputDeckQueryResult finalInputDeck = queryLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Found input deck result xid={} name={} file={}", finalInputDeck.getXid(), finalInputDeck.getInputDeckName(),
            finalInputDeck.getInputDeckFile());

    }
}
