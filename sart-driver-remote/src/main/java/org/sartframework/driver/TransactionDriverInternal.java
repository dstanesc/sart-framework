package org.sartframework.driver;

import java.io.IOException;
import java.util.function.Consumer;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.query.DomainQuery;
import org.sartframework.session.SystemSnapshot;

public interface TransactionDriverInternal {

    long nextTransactionInternal() throws IOException;

    void startTransactionInternal(long xid, int isolation) throws IOException;

    void commitTransactionInternal(long xid, long xct) throws IOException;

    void abortTransactionInternal(long xid) throws IOException;
    
    int statusTransactionInternal(long xid) throws IOException;
    
    SystemSnapshot snapshotTransactionInternal(long xid) throws IOException;

    void onStart(Consumer<TransactionStartedEvent> startConsumer, Long xid);

    void onCommit(Consumer<TransactionCommittedEvent> commitConsumer, long xid);

    void onAbort(Consumer<TransactionAbortedEvent> abortConsumer, long xid);
    
    void onComplete(Consumer<TransactionCompletedEvent> completeConsumer, long xid);
    
    void onConflict(Consumer<ConflictResolvedEvent> conflictConsumer, Long xid);
    
    <T extends DomainEvent<? extends DomainCommand>> void onProgress(Consumer<T> progressConsumer, Class<T> eventType,  long xid);

    <T extends DomainEvent<? extends DomainCommand>> void onCompensate(Consumer<T> compensateConsumer, Class<T> eventType, long xid);
    
    <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer, Consumer<? super Throwable> errorConsumer, Runnable onComplete);
    
    <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer, Runnable onComplete);
    
    <R, Q extends DomainQuery> void onQuery(long xid, int isolation, SystemSnapshot systemSnapshot, boolean subscribe, Q domainQuery, Class<R> resultType, Consumer<R> resultConsumer);
    
    <C extends DomainCommand> void sendCommand(C domainCommand);
}
