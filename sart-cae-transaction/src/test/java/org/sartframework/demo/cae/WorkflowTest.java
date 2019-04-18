package org.sartframework.demo.cae;

import static org.junit.Assert.fail;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.junit.Test;
import org.sartframework.command.DomainCommand;
import org.sartframework.demo.cae.client.RestSimulationApi;
import org.sartframework.demo.cae.command.InputDeckAddResultCommand;
import org.sartframework.demo.cae.command.InputDeckCreateCommand;
import org.sartframework.demo.cae.event.InputDeckCreatedEvent;
import org.sartframework.demo.cae.event.InputDeckResultAddedEvent;
import org.sartframework.driver.DefaultRestTransactionDriver;
import org.sartframework.driver.DomainTransaction;
import org.sartframework.driver.RestTransactionApi;
import org.sartframework.driver.TransactionDriver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WorkflowTest extends AbstractCaeTest {

    final static Logger LOGGER = LoggerFactory.getLogger(WorkflowTest.class);

    @Test
    public void testWorkflowSingleTransaction() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerCommandApi(new RestSimulationApi()).attachTraces().init();

        DomainTransaction domainTransaction = driver.createDomainTransaction();

        CompletableFuture<TestStatus> completionLock = new CompletableFuture<>();

        CompletableFuture<InputDeckResultAddedEvent> progressLock = new CompletableFuture<>();

        String j = nextInputDeckIdentity();

        domainTransaction

            .onAbort(abortEvent -> {

                LOGGER.info("Transaction aborted");

                completionLock.complete(TestStatus.ABORTED);
            })

            .onCommit(commitEvent -> {

                LOGGER.info("Transaction commited");

                completionLock.complete(TestStatus.COMMITTED);
            })

            .onProgress(inputDeckCreated -> {

                LOGGER.info("InputDeckCreatedEvent event detected {}", inputDeckCreated.getInputDeckName());

                Supplier<? extends DomainCommand> simulationResult = executeSimulation(inputDeckCreated);

                domainTransaction.appendCommand(simulationResult);

                domainTransaction.commit();

            }, InputDeckCreatedEvent.class)

            .onProgress(resultAdded -> {

                LOGGER.info("InputDeckResultAddedEvent event detected {}", resultAdded.getResultName());

                progressLock.complete(resultAdded);

            }, InputDeckResultAddedEvent.class)

            .start()

            .appendCommand(() -> {

                return new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j);
            });

        InputDeckResultAddedEvent atomicEvent = progressLock.get(20, TimeUnit.SECONDS);

        TestStatus status = completionLock.get(20, TimeUnit.SECONDS);

        if (TestStatus.COMMITTED != status) {

            fail("Unexpected status");
        }
    }

    private Supplier<? extends DomainCommand> executeSimulation(InputDeckCreatedEvent inputDeckCreated) {

        String resultId = nextResultIdentity();

        String inputDeckId = inputDeckCreated.getAggregateKey();

        String inputDeckFile = inputDeckCreated.getInputDeckFile();

        for (int i = 0; i < 10; i++) {

            LOGGER.info("Simulation processing {} step {} ", inputDeckFile, i);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        return () -> new InputDeckAddResultCommand(inputDeckId, 0, resultId, "result-name-" + resultId, "result-file-" + resultId);
    }

    @Test
    public void testWorkflowMultipleTransactions() throws Exception {

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerCommandApi(new RestSimulationApi()).attachTraces().init();

        CompletableFuture<TestStatus> completionLock = new CompletableFuture<>();

        CompletableFuture<InputDeckResultAddedEvent> progressLock = new CompletableFuture<>();

        String j = nextInputDeckIdentity();

        driver.createDomainTransaction()

            .onAbort(abortEvent -> {

                LOGGER.info("InputDeckCreateCommand Transaction aborted");

                completionLock.complete(TestStatus.ABORTED);
            })

            .onCommit(commitEvent -> {

                LOGGER.info("InputDeckCreateCommand Transaction committed");

                completionLock.complete(TestStatus.COMMITTED);
            })

            .onProgress(inputDeckCreated -> {

                LOGGER.info("InputDeckCreatedEvent event detected {}", inputDeckCreated.getInputDeckName());

                executeExternalSimulation(inputDeckCreated, resultAdded -> {

                    LOGGER.info("Result added ");

                    progressLock.complete(resultAdded);
                });

            }, InputDeckCreatedEvent.class)

            .executeCommand(() -> {

                return new InputDeckCreateCommand(j, "input-deck-name-" + j, "input-deck-file-" + j);
            });

        InputDeckResultAddedEvent atomicEvent = progressLock.get(20, TimeUnit.SECONDS);

        TestStatus status = completionLock.get(20, TimeUnit.SECONDS);

        if (TestStatus.COMMITTED != status) {

            fail("Unexpected status");
        }
    }

    private void executeExternalSimulation(InputDeckCreatedEvent inputDeckCreated, Consumer<InputDeckResultAddedEvent> resultAddedConsumer) {

        CompletableFuture<InputDeckResultAddedEvent> progressLock = new CompletableFuture<>();

        TransactionDriver driver = new DefaultRestTransactionDriver().registerTransactionApi(new RestTransactionApi())
            .registerCommandApi(new RestSimulationApi()).attachTraces().init();

        String resultId = nextResultIdentity();

        String inputDeckId = inputDeckCreated.getAggregateKey();

        String inputDeckFile = inputDeckCreated.getInputDeckFile();

        for (int i = 0; i < 5; i++) {

            LOGGER.info("Simulation processing {} step {} ", inputDeckFile, i);

            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        driver.createDomainTransaction()

            .onProgress(resultAdded -> {

                LOGGER.info("InputDeckResultAddedEvent event detected {}", resultAdded.getResultName());

                progressLock.complete(resultAdded);
                
                resultAddedConsumer.accept(resultAdded);

            }, InputDeckResultAddedEvent.class)

            .executeCommand(() -> new InputDeckAddResultCommand(inputDeckId, 0, resultId, "result-name-" + resultId, "result-file-" + resultId));

        try {
            progressLock.get(10, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }

}
