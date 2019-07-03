package org.sartframework.demo.cae;

import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

import org.junit.Assert;
import org.junit.Test;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.command.InputDeckDeleteCommand;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.TransactionDriver;
import org.sartframework.error.transaction.SystemFault;
import org.sartframework.transaction.TraceDetailFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SystemFaultTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(SystemFaultTest.class);

    @Test
    public void testSystemFaultMissingCreation() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerCommandApi(new RestSimulationApi()).registerDetailFactory(new TraceDetailFactory()).init();

        CompletableFuture<TestStatus> completionLock = new CompletableFuture<>();

        CompletableFuture<SystemFault> errorLock = new CompletableFuture<>();

        String i = nextInputDeckIdentity();

        DomainTransaction domainTransaction = driver.createDomainTransaction();

        domainTransaction

            /*
             * Define behavior on abort
             */

            .onAbort(abortEvent -> {

                LOGGER.info("Transaction aborted");

                completionLock.complete(TestStatus.ABORTED);
            })

            /*
             * Define behavior on commit
             */

            .onCommit(commitEvent -> {

                LOGGER.info("Transaction commited");

                completionLock.complete(TestStatus.COMMITTED);
            })

            /*
             * Handle system fault
             */

            .onSystemFault(systemFault -> {

                LOGGER.info("System fault received {}", systemFault.getThrowable());

                errorLock.complete(systemFault);

            })

            /*
             * execute ... includes .start() and .commit()
             * 
             * Delete non-existing / wrong input deck, should trigger a system
             * fault
             */

            .executeCommandStream(() -> Stream.of(
                new InputDeckCreateCommand(i, "input-deck-name-" + i, "input-deck-file-" + i),
                new InputDeckDeleteCommand(i + "-altered-identity", 0L)));

        TestStatus status = completionLock.get(10, TimeUnit.SECONDS);

        LOGGER.info("Transaction status is {}", status);

        if (TestStatus.ABORTED != status) {
            fail("Unexpected status " + status);
        } else {
            LOGGER.info("Success, aborted transaction found");
        }

        SystemFault systemFault = errorLock.get(10, TimeUnit.SECONDS);

        Throwable originalThrowable = systemFault.getThrowable();
        
        Assert.assertEquals("Throwable", originalThrowable.getClass().getSimpleName());
    }
}
