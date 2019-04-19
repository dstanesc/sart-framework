package org.sartframework.transaction;

import org.sartframework.aggregate.Publisher;
import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.EventDescriptor;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionDetailsAttachedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.session.SystemSnapshot;
import org.sartframework.session.SystemTransaction;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface BusinessTransactionManager extends Publisher {
    
    public final int STATUS_NOT_SET = -1;
    
    public final int STATUS_CREATED = 1;
    
    public final int STATUS_RUNNING = 2;
    
    public final int STATUS_ABORTING = 4;
    
    public final int STATUS_COMMITTED = 8;

    public final int STATUS_ABORTED = 64;
    
    SystemTransaction nextTransaction();
    
    int status(long xid);
    
    SystemSnapshot systemSnapshot(long xid);

    void createTransaction(long xid);
    
    void startTransaction(long xid, int isolation);

    void commitTransaction(long xid, long xct);

    void abortTransaction(long xid);

    Flux<DomainEvent<? extends DomainCommand>> transactionProgressEvents(long xid);
    
    Flux<DomainEvent<? extends DomainCommand>> transactionCompensationEvents(long xid);
    
    Mono<TransactionCompletedEvent> completeListener(long xid);
    
    Mono<TransactionCommittedEvent> commitListener(long xid);
    
    Mono<TransactionAbortedEvent> abortListener(long xid);

    Mono<TransactionStartedEvent> startListener(long xid);

    Flux<TransactionDetailsAttachedEvent> detailsAttachedListener(long xid);
    
    Flux<ConflictResolvedEvent> conflictListener(long xid);
    
    Flux<EventDescriptor> eventDescriptor();
}