package org.sartframework.transaction;

import java.io.Closeable;

import org.sartframework.command.DomainCommand;
import org.sartframework.error.DomainError;
import org.sartframework.error.transaction.TransactionError;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.ProgressLoggedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionDetailsAttachedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.MonoProcessor;
import reactor.core.publisher.ReplayProcessor;

public class TransactionMonitors implements Closeable {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionMonitors.class);

    MonoProcessor<TransactionStartedEvent> startMonitor = MonoProcessor.<TransactionStartedEvent> create();

    MonoProcessor<TransactionAbortedEvent> abortMonitor = MonoProcessor.<TransactionAbortedEvent> create();

    MonoProcessor<TransactionCommittedEvent> commitMonitor = MonoProcessor.<TransactionCommittedEvent> create();

    MonoProcessor<TransactionCompletedEvent> completeMonitor = MonoProcessor.<TransactionCompletedEvent> create();
    
    ReplayProcessor<TransactionDetailsAttachedEvent> detailsAttachedMonitor = ReplayProcessor.<TransactionDetailsAttachedEvent> create();

    ReplayProcessor<ConflictResolvedEvent> conflictResolvedMonitor = ReplayProcessor.<ConflictResolvedEvent> create();

    ReplayProcessor<DomainEvent<? extends DomainCommand>> compensateMonitor = ReplayProcessor.<DomainEvent<? extends DomainCommand>> create();

    ReplayProcessor<DomainEvent<? extends DomainCommand>> progressMonitor = ReplayProcessor.<DomainEvent<? extends DomainCommand>> create();
    
    final Long xid;
    
    public TransactionMonitors(Long xid) {
       this.xid = xid;
    }

    public Long getXid() {
        return xid;
    }

    public void onNextStart(TransactionStartedEvent e) {
        startMonitor().onNext(e);
    }

    public void onNextAbort(TransactionAbortedEvent e) {
        abortMonitor().onNext(e);
    }

    public void onNextCommit(TransactionCommittedEvent e) {
        commitMonitor().onNext(e);
    }

    public void onNextComplete(TransactionCompletedEvent e) {
        completeMonitor().onNext(e);
    }

    public void onNextDetailsAttached(TransactionDetailsAttachedEvent e) {
        detailsAttachedMonitor().onNext(e);
    }
    
    public void onNextConflict(ConflictResolvedEvent e) {
        conflictResolvedMonitor().onNext(e);
    }

    public void onNextLogged(ProgressLoggedEvent e) {
        
        long xcs = e.getXcs();
        
        if (xcs < 0) {
            compensateMonitor().onNext(e.getDomainEvent());
        } else if (xcs > 0) {
            progressMonitor().onNext(e.getDomainEvent());
        } else
            throw new UnsupportedOperationException(" Invalid xcs " + xcs);
    }
    
  
   
    public MonoProcessor<TransactionStartedEvent> startMonitor() {
        return startMonitor;
    }

    public MonoProcessor<TransactionAbortedEvent> abortMonitor() {
        return abortMonitor;
    }

    public MonoProcessor<TransactionCommittedEvent> commitMonitor() {
        return commitMonitor;
    }

    public MonoProcessor<TransactionCompletedEvent> completeMonitor() {
        return completeMonitor;
    }
    
    public ReplayProcessor<TransactionDetailsAttachedEvent> detailsAttachedMonitor() {
        return detailsAttachedMonitor;
    }

    public ReplayProcessor<ConflictResolvedEvent> conflictResolvedMonitor() {
        return conflictResolvedMonitor;
    }

    public ReplayProcessor<DomainEvent<? extends DomainCommand>> compensateMonitor() {
        return compensateMonitor;
    }

    public ReplayProcessor<DomainEvent<? extends DomainCommand>> progressMonitor() {
        return progressMonitor;
    }
    
   
  
    @Override
    public void close() {

        startMonitor().dispose();
        abortMonitor().dispose();
        commitMonitor().dispose();
        completeMonitor().dispose();
        detailsAttachedMonitor().dispose();
        conflictResolvedMonitor().dispose();
        compensateMonitor().dispose();
        progressMonitor().dispose();
    }

}
