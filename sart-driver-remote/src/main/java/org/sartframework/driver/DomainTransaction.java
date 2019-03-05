package org.sartframework.driver;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.sartframework.command.BatchDomainCommand;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionStatus;
import org.sartframework.command.transaction.TransactionStatus.Isolation;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.query.DomainQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public interface DomainTransaction extends TransactionStatus {

    final static Logger LOGGER = LoggerFactory.getLogger(DomainTransaction.class);
    
    TransactionDriver getDriver();
    
    Isolation getIsolation();
    
    DomainTransaction setIsolation(Isolation isolation);
    
    DomainTransaction next();

    DomainTransaction start();

    DomainTransaction commit();

    DomainTransaction abort();

    DomainTransaction appendCommand(Supplier<? extends DomainCommand> commandSupplier);
    
    DomainTransaction onStart(Consumer<TransactionStartedEvent> startConsumer);

    DomainTransaction onCommit(Consumer<TransactionCommittedEvent> commitConsumer);

    DomainTransaction onAbort(Consumer<TransactionAbortedEvent> abortConsumer);
    
    DomainTransaction onComplete(Consumer<TransactionCompletedEvent> completeConsumer);

    DomainTransaction onConflict(Consumer<ConflictResolvedEvent> conflictConsumer);

    <T extends DomainEvent<? extends DomainCommand>> DomainTransaction onProgress(Consumer<T> progressConsumer, Class<T> eventType);

    <T extends DomainEvent<? extends DomainCommand>> DomainTransaction onCompensate(Consumer<T> compensateConsumer, Class<T> eventType);
    
    AtomicInteger getCommandSequenceCounter();

    default DomainTransaction executeCommand(Supplier<DomainCommand> commandSupplier) {

        DomainCommand clientCommand = commandSupplier.get();

        return executeCommandStream(() -> {

            return Stream.of(clientCommand);
        });
    }

    default DomainTransaction executeCommandStream(Supplier<Stream<? extends DomainCommand>> commandSupplier) {

        Stream<? extends DomainCommand> commands = commandSupplier.get();

        LOGGER.info("got transaction {}", getXid());

        try {

            start();

            LOGGER.info("started transaction {}", getXid());

            commands.forEachOrdered(domainCommand -> {

                appendCommand(()->domainCommand);
            });

            commit();

            LOGGER.info("commit transaction requested {}", getXid());

            return this;

        } catch (Throwable t) {

            LOGGER.info("abort transaction requested {}", getXid());

            LOGGER.error("transaction error", t);

            abort();

            throw t;
        }
    }
    
    default DomainTransaction appendCommandStream(Supplier<Stream<? extends DomainCommand>> commandSupplier) {

        Stream<? extends DomainCommand> commands = commandSupplier.get();

        LOGGER.info("got transaction {}", getXid());

        try {

            LOGGER.info("started transaction {}", getXid());

            commands.forEachOrdered(domainCommand -> {

                appendCommand(()->domainCommand);
            });

            LOGGER.info("commit transaction requested {}", getXid());

            return this;

        } catch (Throwable t) {

            LOGGER.error("transaction error", t);

            throw t;
        }
    }
    

    default <C extends DomainCommand> DomainTransaction executeCommandBatch(Supplier<? extends BatchDomainCommand<C>> commandSupplier) {
       
        BatchDomainCommand<C> batchDomainCommand = commandSupplier.get();
        
        start();
        
        LOGGER.info("started transaction {}", getXid());

        AtomicInteger xcsCounter = new AtomicInteger(0);
        
        batchDomainCommand.setXid(getXid());
        
        for (DomainCommand domainCommand : batchDomainCommand) {
            domainCommand.setXid(getXid());
            domainCommand.setXcs(xcsCounter.incrementAndGet());
        }

        appendCommand(()->batchDomainCommand);
        
        commit();

        LOGGER.info("commit transaction requested {}", getXid());

        return this;
    }
    
    
    <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer);

    <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer, Runnable onComplete);

    <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer,
                                            Runnable onComplete);
    
    
    default <R, Q extends DomainQuery> void executeQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer) {
        
        onStartQuery(subscribe, domainQuery, resultType, resultConsumer);
        
        start();
        
        commit();
    }

    default <R, Q extends DomainQuery> void executeQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer, Runnable onComplete) {
        
        onStartQuery(subscribe, domainQuery, resultType, resultConsumer, onComplete);
        
        start();
        
        commit();
    }

    default <R, Q extends DomainQuery> void executeQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer,
                                                         Runnable onComplete) {
        onStartQuery(subscribe, domainQuery, resultType, resultConsumer, errorConsumer, onComplete);
        
        start();
        
        commit();
    }

    default DomainTransaction serialTransaction() {
        
        return serialTransaction(Isolation.READ_SNAPSHOT);
    }

    default DomainTransaction serialTransaction(Isolation isolation){

        CompletableFuture<TransactionCompletedEvent> chainLock = new CompletableFuture<>();

        onComplete(completeEvent -> {
            
            LOGGER.info("Triggered transaction xid={} onComplete with status {} ", completeEvent.getXid(), completeEvent.getStatus());
            
            chainLock.complete(completeEvent);
        });
        
        try {

            TransactionCompletedEvent completeEvent = chainLock.get();
            
            LOGGER.info("Transaction xid={} completed with {}", completeEvent.getStatus());

        } catch (InterruptedException | ExecutionException e) {

            throw new RuntimeException(e);
        }

        return getDriver().createDomainTransaction(isolation);
    }
}
