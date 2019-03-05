package org.sartframework.transaction.kafka;

import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.transaction.DomainEventCompensatedEvent;
import org.sartframework.event.transaction.ProgressLoggedEvent;
import org.sartframework.event.transaction.TransactionAbortRequestedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommitRequestedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCreatedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.transaction.GenericTransactionAggregate;
import org.sartframework.transaction.kafka.services.TransactionRollbackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaTransactionAggregate extends GenericTransactionAggregate {

    final static Logger LOGGER = LoggerFactory.getLogger(KafkaTransactionAggregate.class);
    

    @Override
    protected void dispatch(DomainCommand domainCommand) {
        
        LOGGER.info("KafkaBusinessTransactionManager publish domain command {} ", domainCommand);
        
        KafkaBusinessTransactionManager.get().publish(domainCommand);
    }

    
    
    @Override
    protected void dispatch(TransactionCommand transactionCommand) {
        
        LOGGER.info("KafkaBusinessTransactionManager publish transaction command {} ", transactionCommand);
        
        KafkaBusinessTransactionManager.get().publish(transactionCommand);
    }



    @Override
    protected void dispatch(TransactionEvent transactionEvent) {
        
        LOGGER.info("Dispatching transaction event {} ", transactionEvent);

        if (transactionEvent instanceof TransactionCreatedEvent)
            handleTransactionCreatedEvent((TransactionCreatedEvent) transactionEvent);
        else if (transactionEvent instanceof TransactionStartedEvent)
            handleTransactionStartedEvent((TransactionStartedEvent) transactionEvent);
        else if (transactionEvent instanceof TransactionCommitRequestedEvent)
            handleTransactionCommitRequestedEvent((TransactionCommitRequestedEvent) transactionEvent);
        else if (transactionEvent instanceof TransactionAbortRequestedEvent)
            handleTransactionAbortRequestedEvent((TransactionAbortRequestedEvent) transactionEvent);
        else if (transactionEvent instanceof TransactionCommittedEvent)
            handleTransactionCommittedEvent((TransactionCommittedEvent) transactionEvent);
        else if (transactionEvent instanceof TransactionAbortedEvent)
            handleTransactionAbortedEvent((TransactionAbortedEvent) transactionEvent);
        else if (transactionEvent instanceof ProgressLoggedEvent)
            handleProgressLoggedEvent((ProgressLoggedEvent) transactionEvent);
        else if (transactionEvent instanceof DomainEventCompensatedEvent)
            handleDomainEventCompensatedEvent((DomainEventCompensatedEvent) transactionEvent);
        else
            throw new UnsupportedOperationException();

        LOGGER.info("KafkaBusinessTransactionManager publish event {} ", transactionEvent);
        
        KafkaBusinessTransactionManager.get().publish(transactionEvent);
    }

//    @Override
//    public void handleAbortTransactionCommand(AbortTransactionCommand abortTransactionCommand) {
//        
//        KafkaBusinessTransactionManager businessTransactionManager = KafkaBusinessTransactionManager.get();
//        
//        LOGGER.info("Starting TransactionRollbackService for {} ", getXid() );
//        
//        TransactionRollbackService transactionRollbackService = new TransactionRollbackService(getXid(), businessTransactionManager.getKafkaStreamsConfiguration()).start();
//        
//        businessTransactionManager.registerRollbackService(transactionRollbackService);
//        
//        super.handleAbortTransactionCommand(abortTransactionCommand);
//    }

    
    @Override
    public void handleTransactionAbortRequestedEvent(TransactionAbortRequestedEvent e) {
        
        super.handleTransactionAbortRequestedEvent(e);
        
        KafkaBusinessTransactionManager businessTransactionManager = KafkaBusinessTransactionManager.get();
        
        LOGGER.info("Starting TransactionRollbackService for {} ", getXid());
        
        PartitionOffset startOffset = new PartitionOffset(getPartition(), getOffset());

        TransactionRollbackService transactionRollbackService = new TransactionRollbackService(getXid(), startOffset, businessTransactionManager.getKafkaStreamsConfiguration()).start();
    
        businessTransactionManager.registerRollbackService(transactionRollbackService);
    }

    
    @Override
    public void handleTransactionAbortedEvent(TransactionAbortedEvent e) {
        
        LOGGER.info("Stopping TransactionRollbackService for {} ", getXid() );
       
        KafkaBusinessTransactionManager.get().unregisterRollbackService(e.getXid());
        
        //FIXME TOO early !!!!
        //KafkaBusinessTransactionManager.get().unregisterTransactionMonitors(e.getXid());
        // unregisterRollbackService
        super.handleTransactionAbortedEvent(e);
    }


    @Override
    public void handleTransactionCommittedEvent(TransactionCommittedEvent e) {
        
        //FIXME TOO early !!!!
        //KafkaBusinessTransactionManager.get().unregisterTransactionMonitors(e.getXid());
        
        super.handleTransactionCommittedEvent(e);
        
    }
    
}
