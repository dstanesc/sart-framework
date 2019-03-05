package org.sartframework.aggregate;

import org.sartframework.annotation.AsynchHandlerDelegator;
import org.sartframework.annotation.DomainCommandHandler;
import org.sartframework.annotation.DomainEventHandler;
import org.sartframework.annotation.SynchHandlerDelegator;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.VoidDomainCommand;
import org.sartframework.command.transaction.LogProgressCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.GenericVoidDomainEvent;
import org.sartframework.event.TransactionEvent;



public abstract class AnnotatedDomainAggregate extends GenericDomainAggregate implements CommandHandlingAggregate {

//    private transient Consumer<DomainEvent<? extends DomainCommand>> domainEventChannel;
//
//    private transient Consumer<DomainCommand> domainCommandChannel;
//
//    private transient Consumer<TransactionCommand> transactionCommandChannel;
//
//    private transient Consumer<TransactionEvent> transactionEventChannel;
//
//    public AnnotatedDomainAggregate setDomainEventChannel(Consumer<DomainEvent<? extends DomainCommand>> domainEventChannel) {
//        this.domainEventChannel = domainEventChannel;
//        return this;
//    }
//
//    public AnnotatedDomainAggregate setDomainCommandChannel(Consumer<DomainCommand> domainCommandChannel) {
//        this.domainCommandChannel = domainCommandChannel;
//        return this;
//    }
//
//    public AnnotatedDomainAggregate setTransactionCommandChannel(Consumer<TransactionCommand> transactionCommandChannel) {
//        this.transactionCommandChannel = transactionCommandChannel;
//        return this;
//    }
//
//    public AnnotatedDomainAggregate setTransactionEventChannel(Consumer<TransactionEvent> transactionEventChannel) {
//        this.transactionEventChannel = transactionEventChannel;
//        return this;
//    }
//
    
    
    private transient Publisher publisher;
    
    @Override
    public void publish(TransactionCommand transactionCommand) {
        publisher.publish(transactionCommand);
       // transactionCommandChannel.accept(transactionCommand);
    }

    @Override
    public void publish(TransactionEvent transactionEvent) {
       publisher.publish(transactionEvent);
       // transactionEventChannel.accept(transactionEvent);
    }

    @Override
    public void publish(DomainCommand atomicCommand) {
        publisher.publish(atomicCommand);
       // domainCommandChannel.accept(atomicCommand);
    }

    @Override
    public void publish(DomainEvent<? extends DomainCommand> domainEvent) {
        //domainEventChannel.accept(domainEvent);
        publisher.publish(domainEvent);
    }

    public AnnotatedDomainAggregate setPublisher(Publisher publisher) {
        this.publisher = publisher;
        return this;
    }

    @Override
    public long handle(DomainEvent<? extends DomainCommand> domainEvent) {

        return SynchHandlerDelegator.<DomainEvent<? extends DomainCommand>, DomainEventHandler, Long> wrap(this, DomainEventHandler.class).handle(domainEvent);
    }

    
    
    @Override
    public void handle(DomainCommand domainCommand) {

        if (domainCommand instanceof VoidDomainCommand) {

            handleVoidCommand((VoidDomainCommand) domainCommand);

        } else {

            AsynchHandlerDelegator.<DomainCommand, DomainCommandHandler> wrap(this, DomainCommandHandler.class).handle(domainCommand);
        }
    }

    @Override
    public void handleVoidCommand(VoidDomainCommand domainCommand) {
        
        DomainEvent<VoidDomainCommand> domainEvent = new GenericVoidDomainEvent(domainCommand.getAggregateKey(), domainCommand.getAggregateVersion()).addTransactionHeader(domainCommand.getXid(), domainCommand.getXcs());
        domainEvent.setTargetAggregateVersion(domainCommand.getAggregateVersion());
        // no local dispatching needed, just progress logging for transaction aborting needed
        publish(new LogProgressCommand(domainCommand.getXid(), domainCommand.getXcs(), domainEvent));
    
    }
    
    
}
