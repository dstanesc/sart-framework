package org.sartframework.driver;

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
import org.sartframework.session.SystemSnapshot;
import org.sartframework.session.SystemTransaction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultDomainTransaction implements DomainTransaction {

    final static Logger LOGGER = LoggerFactory.getLogger(DefaultDomainTransaction.class);

    private TransactionDriverInternal transactionDriverInternal;

    private SystemTransaction localTransaction;

    private Isolation isolation;

    AtomicInteger commandSequenceCounter = new AtomicInteger(0);

    public DefaultDomainTransaction(TransactionDriverInternal transactionDriverInternal) {
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

    protected int getIsolationNumber() {
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

        if (localTransaction == null)
            throw new TransactionNotStartedException();

        return localTransaction.getXid();
    }
    

    public String getSid() {

        if (localTransaction == null)
            throw new TransactionNotStartedException();

        return localTransaction.getSid();
    }
    

    @Override
    public Status getStatus() {

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
    }

    @Override
    public <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType,
                                                                     Consumer<R> resultConsumer) {

        onStart(startEvent -> {

            LOGGER.info("System Snapshot for xid={} snap={}", startEvent.getXid(), startEvent.getSystemSnapshot());

            TransactionDriverInternal driver = getTransactionDriver();
            
            driver.onQuery(getXid(), getIsolationNumber(), getSystemSnapshot(startEvent), subscribe, domainQuery, resultType, resultConsumer, null, null);
            
        });

        return this;
    }

    @Override
    public <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType,
                                                                     Consumer<R> resultConsumer, Runnable onComplete) {
        onStart(startEvent -> {

            LOGGER.info("System Snapshot for xid={} snap={}", startEvent.getXid(), startEvent.getSystemSnapshot());

            TransactionDriverInternal driver = getTransactionDriver();
            
            driver.onQuery(getXid(), getIsolationNumber(), getSystemSnapshot(startEvent), subscribe, domainQuery, resultType, resultConsumer, null, onComplete);
        });

        return this;
    }

    @Override
    public <R, Q extends DomainQuery> DomainTransaction onStartQuery(boolean subscribe, Q domainQuery, Class<R> resultType,
                                                                     Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer,
                                                                     Runnable onComplete) {
        onStart(startEvent -> {

            LOGGER.info("System Snapshot for xid={} snap={}", startEvent.getXid(), startEvent.getSystemSnapshot());

            TransactionDriverInternal driver = getTransactionDriver();
            
            driver.onQuery(getXid(), getIsolationNumber(), getSystemSnapshot(startEvent), subscribe, domainQuery, resultType, resultConsumer, errorConsumer, onComplete);
        });

        return this;
    }

    protected SystemSnapshot getSystemSnapshot(TransactionStartedEvent startEvent) {
        switch (getIsolation()) {
            case READ_UNCOMMITTED:
                return startEvent.getSystemSnapshot();
            case READ_SNAPSHOT:
                return startEvent.getSystemSnapshot();
            case READ_COMMITTED:
                return getTransactionDriver().snapshotTransactionInternal(getXid());
            default:
                throw new IllegalStateException("Unsupported isolation level : " + getIsolation());
        }
    }

    @Override
    public DomainTransaction next() {

        this.localTransaction = getTransactionDriver().nextTransactionInternal();

        return this;
    }

    @Override
    public DomainTransaction start() {

        getTransactionDriver().startTransactionInternal(getXid(), getIsolationNumber());

        return this;
    }

    @Override
    public DomainTransaction commit() {

        LOGGER.info("Committing xid={} ", getXid());

        getTransactionDriver().commitTransactionInternal(getXid(), getCommandSequenceCounter().get());

        return this;

    }

    @Override
    public DomainTransaction abort() {

        getTransactionDriver().abortTransactionInternal(getXid());

        return this;
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

    @Override
    public TransactionDriver getDriver() {

        return transactionDriverInternal;
    }

}
