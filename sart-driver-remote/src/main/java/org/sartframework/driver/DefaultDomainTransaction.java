package org.sartframework.driver;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.query.DomainQuery;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDomainTransaction implements DomainTransaction {

    final static Logger LOGGER = LoggerFactory.getLogger(DefaultDomainTransaction.class);

    private TransactionDriverInternal transactionDriverInternal;
    
    private TransactionDriver transactionDriver;

    private Long xid;

    private Isolation isolation = Isolation.READ_SNAPSHOT;
    
    AtomicInteger commandSequenceCounter = new AtomicInteger(0);

    public DefaultDomainTransaction(TransactionDriver transactionDriver, TransactionDriverInternal transactionDriverInternal) {
        this.transactionDriver = transactionDriver;
        this.transactionDriverInternal = transactionDriverInternal;
    }

    @Override
    public AtomicInteger getCommandSequenceCounter() {

        return commandSequenceCounter;
    }

    @Override
    public DomainTransaction setIsolation(Isolation isolation) {
        this.isolation = isolation;
        return this;
    }

    @Override
    public Isolation getIsolation() {
        return isolation;
    }

    private int getIsolationNumber() {
        switch (isolation) {
            case READ_UNCOMMITTED:
                return 1;
            case READ_COMMITTED:
                return 2;
            case READ_SNAPSHOT:
                return 4;
            default:
                throw new UnsupportedOperationException("Unknown isolation level");
        }
    }

    @Override
    public long getXid() {

        if (xid == null)
            throw new TransactionNotStartedException();

        return xid;
    }

    @Override
    public Status getStatus() {

        try {

            int status = getTransactionDriver().statusTransactionInternal(getXid());

            switch (status) {
                case 1:
                    return Status.CREATED;
                case 2:
                    return Status.RUNNING;
                case 8:
                    return Status.COMMITED;
                case 64:
                    return Status.ABORTED;
                default:
                    return Status.UNKNOWN;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer) {

        onStart(startEvent -> {

            LOGGER.info("System Snapshot for xid={} snap={}", startEvent.getXid(), startEvent.getSystemSnapshot());

            getTransactionDriver().onQuery(getXid(), getIsolationNumber(), startEvent.getSystemSnapshot(), subscribe, domainQuery, resultType,
                resultConsumer);
        });

        return this;
    }

    @Override
    public <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer,
                                                                Runnable onComplete) {
        onStart(startEvent -> {

            LOGGER.info("System Snapshot for xid={} snap={}", startEvent.getXid(), startEvent.getSystemSnapshot());

            getTransactionDriver().onQuery(getXid(), getIsolationNumber(), startEvent.getSystemSnapshot(), subscribe, domainQuery, resultType,
                resultConsumer, onComplete);
        });

        return this;
    }

    @Override
    public <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer,
                                                                Consumer<? super Throwable> errorConsumer, Runnable onComplete) {
        onStart(startEvent -> {

            LOGGER.info("System Snapshot for xid={} snap={}", startEvent.getXid(), startEvent.getSystemSnapshot());

            getTransactionDriver().onQuery(getXid(), getIsolationNumber(), startEvent.getSystemSnapshot(), subscribe, domainQuery, resultType,
                resultConsumer, errorConsumer, onComplete);
        });

        return this;
    }

    @Override
    public DomainTransaction next() {

        try {

            this.xid = getTransactionDriver().nextTransactionInternal();

            return this;

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public DomainTransaction start() {

        try {

            getTransactionDriver().startTransactionInternal(getXid(), getIsolationNumber());

            return this;

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public DomainTransaction commit() {

        try {
            
            LOGGER.info("Committing xid={} ", getXid());
            
            getTransactionDriver().commitTransactionInternal(getXid(), getCommandSequenceCounter().get());

            return this;

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public DomainTransaction abort() {

        try {

            getTransactionDriver().abortTransactionInternal(getXid());

            return this;

        } catch (IOException e) {

            throw new RuntimeException(e);
        }
    }

    @Override
    public DomainTransaction appendCommand(Supplier<? extends DomainCommand> commandSupplier) {
        
        int xcs = getCommandSequenceCounter().incrementAndGet();
        
        DomainCommand domainCommand = commandSupplier.get();
        
        LOGGER.info("Appending xid={} xcs={} command={} ", getXid(), xcs, domainCommand);

        domainCommand.setXid(getXid());

        domainCommand.setXcs(xcs);

        getTransactionDriver().sendCommand(domainCommand);

        return this;
    }

    @Override
    public DomainTransaction onStart(Consumer<TransactionStartedEvent> startConsumer) {

        getTransactionDriver().onStart(startConsumer, getXid());

        return this;
    }

    @Override
    public DomainTransaction onCommit(Consumer<TransactionCommittedEvent> commitConsumer) {

        getTransactionDriver().onCommit(commitConsumer, getXid());

        return this;
    }

    @Override
    public DomainTransaction onAbort(Consumer<TransactionAbortedEvent> abortConsumer) {

        getTransactionDriver().onAbort(abortConsumer, getXid());

        return this;
    }

    @Override
    public DomainTransaction onComplete(Consumer<TransactionCompletedEvent> completeConsumer) {

        getTransactionDriver().onComplete(completeConsumer, getXid());

        return this;
    }

    @Override
    public DomainTransaction onConflict(Consumer<ConflictResolvedEvent> conflictConsumer) {

        getTransactionDriver().onConflict(conflictConsumer, getXid());

        return this;
    }

    @Override
    public <T extends DomainEvent<? extends DomainCommand>> DomainTransaction onProgress(Consumer<T> progressConsumer, Class<T> eventType) {

        getTransactionDriver().onProgress(progressConsumer, eventType, getXid());

        return this;
    }

    @Override
    public <T extends DomainEvent<? extends DomainCommand>> DomainTransaction onCompensate(Consumer<T> compensateConsumer, Class<T> eventType) {

        getTransactionDriver().onCompensate(compensateConsumer, eventType, getXid());

        return this;
    }

    public TransactionDriverInternal getTransactionDriver() {

        return transactionDriverInternal;
    }

    public TransactionDriver getDriver() {

        return transactionDriver;
    }
}
