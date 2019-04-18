package org.sartframework.driver;

import java.util.function.Consumer;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionDetailsAttachedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.query.DomainQuery;
import org.sartframework.session.SystemSnapshot;
import org.sartframework.session.SystemTransaction;
import org.sartframework.transaction.TransactionDetails;

public interface TransactionDriverInternal extends TransactionDriver {

    SystemTransaction nextTransactionInternal();

    void startTransactionInternal(long xid, int isolation);

    void commitTransactionInternal(long xid, long xct);

    void abortTransactionInternal(long xid);
    
    int statusTransactionInternal(long xid);
    
    void attachTransactionDetails(long xid, TransactionDetails transactionDetails);
    
    SystemSnapshot snapshotTransactionInternal(long xid);

    void onStart(Consumer<TransactionStartedEvent> startConsumer, Long xid);

    void onCommit(Consumer<TransactionCommittedEvent> commitConsumer, long xid);

    void onAbort(Consumer<TransactionAbortedEvent> abortConsumer, long xid);
    
    void onComplete(Consumer<TransactionCompletedEvent> completeConsumer, long xid);
    
    void onDetailsAttached(Consumer<TransactionDetailsAttachedEvent> detailsConsumer, long xid);
    
    void onConflict(Consumer<ConflictResolvedEvent> conflictConsumer, long xid);
    
    <T extends DomainEvent<? extends DomainCommand>> void onProgress(Consumer<T> progressConsumer, Class<T> eventType,  long xid);

    <T extends DomainEvent<? extends DomainCommand>> void onCompensate(Consumer<T> compensateConsumer, Class<T> eventType, long xid);
    
    <C extends DomainCommand> void sendCommand(C domainCommand);
    
    <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer, Runnable onComplete);
    
}
