package org.sartframework.demo.cae;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.InputDeckAddResultCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckUpdateFileCommand;
import org.sartframework.driver.RestConflictQueryApi;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.transaction.TraceDetailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DurationTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(DurationTest.class);

    
    @Test
    public void testInputDeckPerformanceMonitor() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerQueryApi(new RestConflictQueryApi()).registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();


        CompletableFuture<Long> startLock = new CompletableFuture<>();

        CompletableFuture<Long> completionLock = new CompletableFuture<>();
        
        String j = nextInputDeckIdentity();
        
        String k = nextResultIdentity();
        
        driver.createDomainTransaction()

        .onStart(start -> {

            Long processedTime = start.getCreationTime();

            LOGGER.info("Transaction started @ {}", processedTime);

            startLock.complete(processedTime);
        })

        .onCommit(commit -> {

            Long processedTime = commit.getCreationTime();

            LOGGER.info("Transaction commited @ {}", processedTime);

            completionLock.complete(processedTime);
        })


        .executeCommandStream(() -> Stream.of(new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j),
            new InputDeckAddResultCommand(j, 0, k, "result-name-" + k, "result-file-" + k),
            new InputDeckUpdateFileCommand(j, 0, "input-deck-file-updated-txn")));

        Long completed = completionLock.get(10, TimeUnit.SECONDS);

        Long started = startLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Transaction executed successfully Duration is {} ", (completed - started) + " ms");

        Assert.assertTrue(completed > started);
    }
    
}
