package org.sartframework.transaction;

import javax.persistence.Id;

import org.sartframework.aggregate.TransactionAggregate;
import org.sartframework.command.DefaultVoidDomainCommand;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.AbortTransactionCommand;
import org.sartframework.command.transaction.AttachTransactionDetailsCommand;
import org.sartframework.command.transaction.CommitTransactionCommand;
import org.sartframework.command.transaction.CompensateDomainEventCommand;
import org.sartframework.command.transaction.CreateTransactionCommand;
import org.sartframework.command.transaction.LogProgressCommand;
import org.sartframework.command.transaction.StartTransactionCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.transaction.DomainEventCompensatedEvent;
import org.sartframework.event.transaction.ProgressLoggedEvent;
import org.sartframework.event.transaction.TransactionAbortRequestedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommitRequestedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCreatedEvent;
import org.sartframework.event.transaction.TransactionDetailsAttachedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.session.SystemSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class GenericTransactionAggregate implements TransactionAggregate {

    final static Logger LOGGER = LoggerFactory.getLogger(GenericTransactionAggregate.class);

    @Id
    private Long xid;

    private Long xct;

    private int status;

    private int progressCounter = 0;

    private int compensationCounter = 0;

    private SystemSnapshot systemSnapshot;

    private int isolation;

    private int partition;

    private long offset;
    
    private TransactionDetails details;

    public GenericTransactionAggregate() {
        super();
    }

    public GenericTransactionAggregate(CreateTransactionCommand createTransactionCommand) {
        TransactionCreatedEvent createdEvent = new TransactionCreatedEvent(createTransactionCommand.getXid());
        dispatch(createdEvent);
    }

    public void handleCreateTransactionCommand(CreateTransactionCommand createTransactionCommand) {
        TransactionCreatedEvent createdEvent = new TransactionCreatedEvent(createTransactionCommand.getXid());
        dispatch(createdEvent);
    }

    public void handleStartTransactionCommand(StartTransactionCommand startTransactionCommand) {
        TransactionStartedEvent startedEvent = new TransactionStartedEvent(startTransactionCommand.getXid(), startTransactionCommand.getIsolation(),
            startTransactionCommand.getSystemTransactions());
        dispatch(startedEvent);
    }

    public void handleCommitTransactionCommand(CommitTransactionCommand commitTransactionCommand) {
        TransactionCommitRequestedEvent commitRequestedEvent = new TransactionCommitRequestedEvent(commitTransactionCommand.getXid(),
            commitTransactionCommand.getXct());
        dispatch(commitRequestedEvent);
    }

    public void handleAbortTransactionCommand(AbortTransactionCommand abortTransactionCommand) {
        TransactionAbortRequestedEvent abortRequestedEvent = new TransactionAbortRequestedEvent(abortTransactionCommand.getXid());
        dispatch(abortRequestedEvent);
    }

    public void handleProgressCommand(LogProgressCommand logEventCommand) {
        ProgressLoggedEvent progressEvent = new ProgressLoggedEvent(logEventCommand.getXid(), logEventCommand.getXcs(),
            logEventCommand.getDomainEvent());
        dispatch(progressEvent);
    }

    private void handleAttachDetailsCommand(AttachTransactionDetailsCommand attachDetailsCommand) {
        TransactionDetailsAttachedEvent detailsAttachedEvent = new TransactionDetailsAttachedEvent(attachDetailsCommand.getXid(), attachDetailsCommand.getDetails());
        dispatch(detailsAttachedEvent);
    }
    
    public void handleCompensateCommand(CompensateDomainEventCommand compensateDomainEventCommand) {
        
        DomainEventCompensatedEvent compensatedEvent = new DomainEventCompensatedEvent(compensateDomainEventCommand.getXid(),
            compensateDomainEventCommand.getXcs(), compensateDomainEventCommand.getDomainEvent(), compensateDomainEventCommand.isSkip());
        
        LOGGER.info("Dispatching compensated event {} ", compensatedEvent);
        
        dispatch(compensatedEvent);
    }

    public void handle(TransactionCommand c) {

        LOGGER.info("Handling transaction command {} ", c);
        
        if (c instanceof CreateTransactionCommand) {
            handleCreateTransactionCommand((CreateTransactionCommand) c);
        } else if (c instanceof StartTransactionCommand) {
            handleStartTransactionCommand((StartTransactionCommand) c);
        } else if (c instanceof CommitTransactionCommand) {
            handleCommitTransactionCommand((CommitTransactionCommand) c);
        } else if (c instanceof AbortTransactionCommand) {
            handleAbortTransactionCommand((AbortTransactionCommand) c);
        } else if (c instanceof CompensateDomainEventCommand) {
            handleCompensateCommand((CompensateDomainEventCommand) c);
        } else if (c instanceof LogProgressCommand) {
            handleProgressCommand((LogProgressCommand) c);
        } else if (c instanceof AttachTransactionDetailsCommand) {
            handleAttachDetailsCommand((AttachTransactionDetailsCommand) c);
        } else
            throw new UnsupportedOperationException();
    }

    

    public void handleTransactionCreatedEvent(TransactionCreatedEvent e) {

        LOGGER.info("Creating transaction {} ", e.getXid());

        this.xid = e.getXid();
        this.status = BusinessTransactionManager.STATUS_CREATED;
    }

    public void handleTransactionStartedEvent(TransactionStartedEvent e) {

        LOGGER.info("Starting transaction {} ", e.getXid());

        this.status = BusinessTransactionManager.STATUS_RUNNING;

        this.isolation = e.getIsolation();

        this.systemSnapshot = e.getSystemSnapshot();
    }

    public void handleTransactionCommitRequestedEvent(TransactionCommitRequestedEvent e) {

        LOGGER.info("Transaction commit requested event received {} ", e.getXid());

        if (this.status == BusinessTransactionManager.STATUS_RUNNING) {

            LOGGER.info("Setting status to COMMIT_REQUESTED {} ", e.getXid());

            // this.status = BusinessTransactionManager.STATUS_COMMIT_REQUESTED;
            this.xct = e.getXct();

            tryCommit();

        } else {

            LOGGER.info("Ignore STATUS_COMMIT_REQUESTED {} as status already {}", e.getXid(), this.status);
        }
    }

    public void handleTransactionAbortRequestedEvent(TransactionAbortRequestedEvent e) {

        LOGGER.info("Transaction abort requested event received {} -> {}", e.getXid(), this.status);

        this.status = BusinessTransactionManager.STATUS_ABORTING;
    }

    public void handleTransactionCommittedEvent(TransactionCommittedEvent e) {

        this.status = BusinessTransactionManager.STATUS_COMMITTED;

        LOGGER.info("transaction {} commited", e.getXid());
    }

    public void handleTransactionAbortedEvent(TransactionAbortedEvent e) {

        this.status = BusinessTransactionManager.STATUS_ABORTED;

        LOGGER.info("transaction {} aborted", e.getXid());
    }

    public void handleProgressLoggedEvent(ProgressLoggedEvent loggingEvent) {

        DomainEvent<? extends DomainCommand> atomicEvent = loggingEvent.getDomainEvent();

        long xcs = atomicEvent.getXcs();

        LOGGER.info("Proggress logged event received {} {} ", loggingEvent.getXid(), atomicEvent.getXcs());

        if (xcs < 0) {

            incrementCompensation(atomicEvent);

            tryAbort();

        } else {

            incrementProgress(atomicEvent);

            if (getStatus() == BusinessTransactionManager.STATUS_RUNNING) {

                tryCommit();

            } else if (getStatus() == BusinessTransactionManager.STATUS_ABORTING) {

                // compensate late events which were missed by the
                // TransactionRollbackService
//
//                DomainEvent<? extends DomainCommand> domainEvent = loggingEvent.getDomainEvent();
//
//                CompensateDomainEventCommand compensate = new CompensateDomainEventCommand(xid, xcs, domainEvent);
//
//                dispatch(compensate);
            }
        }
    }

    public void handleTransactionDetailsAttachedEvent(TransactionDetailsAttachedEvent transactionEvent) {
        
        LOGGER.info("Transaction details attached event received {} -> {}", transactionEvent.getXid(), transactionEvent.getDetails());
    
        if(this.details == null) {
            this.details = new TransactionDetails(transactionEvent.getXid());
        }
        
        this.details.addDetails(transactionEvent.getDetails());
    }


    public void handleDomainEventCompensatedEvent(DomainEventCompensatedEvent compensatedEvent) {

        DomainEvent<? extends DomainCommand> domainEvent = compensatedEvent.getDomainEvent();

        long xcs = domainEvent.getXcs();

        LOGGER.info("Domain Event compensated received for {} xid={}, xcs={}", domainEvent, xid, xcs);

        if (xcs > 0) {
            DomainCommand reverseCommand = compensatedEvent.isSkip()
                ? new DefaultVoidDomainCommand(domainEvent.getAggregateKey(), domainEvent.getSourceAggregateVersion())
                    .addTransactionHeader(xid, -xcs)
                : domainEvent.undo(xid, -xcs);

            LOGGER.info("Compensating {} with {}, xcs={} ", domainEvent, reverseCommand, reverseCommand.getXcs());

            dispatch(reverseCommand);
        }
    }

    protected void tryCommit() {

        LOGGER.info("tryCommit with status {}, progressCounter {}=={}", getStatus(), progressCounter(), xct);

        if (getStatus() != BusinessTransactionManager.STATUS_ABORTING && getStatus() != BusinessTransactionManager.STATUS_ABORTED) {
            if (xct != null && progressCounter() == xct) {
                setStatus(BusinessTransactionManager.STATUS_COMMITTED);
                dispatch(new TransactionCommittedEvent(xid));
            }
        }
    }


    protected void tryAbort() {

        LOGGER.info("tryAbort with status {}, progressCounter {}=={}", getStatus(), progressCounter(), compensationCounter());

        if (getStatus() == BusinessTransactionManager.STATUS_ABORTING) {
            // TODO is this condition too simplistic ?, key set equivalence
            // (reciprocal inclusion) is another possibility
            if (progressCounter() == compensationCounter() && progressCounter() > 0) {
                setStatus(BusinessTransactionManager.STATUS_ABORTED);
                dispatch(new TransactionAbortedEvent(xid));
            }
        }
    }

    public Long getXct() {
        return xct;
    }

    public void setXct(Long xct) {
        this.xct = xct;
    }

    public void setXid(Long xid) {
        this.xid = xid;
    }

    public long getXid() {
        return xid;
    }

    public void setXid(long xid) {
        this.xid = xid;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getPartition() {
        return partition;
    }

    public void setPartition(int partition) {
        this.partition = partition;
    }

    public long getOffset() {
        return offset;
    }

    public void setOffset(long offset) {
        this.offset = offset;
    }
    
    public int getIsolation() {
        return isolation;
    }

    public void setIsolation(int isolation) {
        this.isolation = isolation;
    }

    protected void incrementProgress(DomainEvent<? extends DomainCommand> atomicEvent) {

        long xcs = atomicEvent.getXcs();

        LOGGER.info("Increment progressCounter xcs={} for {}", xcs, atomicEvent);

        progressCounter++;
    }

    protected void incrementCompensation(DomainEvent<? extends DomainCommand> atomicEvent) {

        long xcs = atomicEvent.getXcs();

        LOGGER.info("Increment compensationCounter xcs={} for {}", xcs, atomicEvent);

        compensationCounter++;
    }

    protected int progressCounter() {

        return progressCounter;
    }

    protected int compensationCounter() {

        return compensationCounter;
    }

    public SystemSnapshot getSystemSnapshot() {

        return systemSnapshot;
    }

    protected abstract void dispatch(TransactionCommand transactionCommand);

    protected abstract void dispatch(DomainCommand domainCommand);

    protected abstract void dispatch(TransactionEvent transactionEvent);

}
