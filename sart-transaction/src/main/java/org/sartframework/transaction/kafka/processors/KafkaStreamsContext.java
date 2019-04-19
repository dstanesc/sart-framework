package org.sartframework.transaction.kafka.processors;

import org.apache.kafka.streams.processor.ProcessorContext;
import org.sartframework.aggregate.CommandChannel;
import org.sartframework.aggregate.EventChannel;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.TransactionEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaStreamsContext implements CommandChannel, EventChannel {
    
    final static Logger LOGGER = LoggerFactory.getLogger(KafkaStreamsContext.class);
    
    private ProcessorContext context;
    
    private String domainCommandChannel;
    
    private String domainEventChannel;
    
    private String transactionCommandChannel;
    
    private String transactionEventChannel;
    
    
    public KafkaStreamsContext() {
        super();
    }

    @Override
    public void publish(DomainCommand domainCommand) {
        
        String channelName = getDomainCommandChannel();
        
        if(channelName == null) throw new UnsupportedOperationException("DomainCommand channel not configured");
        
        LOGGER.info("KafkaStreamsContext publish domain command {} to {}", domainCommand, channelName);
        
        getContext().forward(domainCommand.getAggregateKey(), domainCommand, channelName /*"domain-command-sink"*/);
    }
    
    @Override
    public void publish(DomainEvent<? extends DomainCommand> domainEvent) {
      
        String channelName = getDomainEventChannel();
        
        if(channelName == null) throw new UnsupportedOperationException("DomainEvent channel not configured");
        
        LOGGER.info("KafkaStreamsContext publish domain event {} to {}", domainEvent, channelName);
        
        getContext().forward(domainEvent.getAggregateKey(), domainEvent, channelName);
    }
    
    @Override
    public void publish(TransactionCommand transactionCommand) {
        
        String channelName = getTransactionCommandChannel();
        
        if(channelName == null) throw new UnsupportedOperationException("TransactionCommand channel not configured");
        
        LOGGER.info("KafkaStreamsContext publish transaction command {} to {}", transactionCommand, channelName);
        
        getContext().forward(transactionCommand.getXid(), transactionCommand, channelName );
    }
    
    @Override
    public void publish(TransactionEvent transactionEvent) {
        
        String channelName = getTransactionEventChannel();
        
        if(channelName == null) throw new UnsupportedOperationException("TransactionEvent channel not configured");
        
        LOGGER.info("KafkaStreamsContext publish transaction event {} to {}", transactionEvent, channelName);
        
        getContext().forward(transactionEvent.getXid(), transactionEvent, channelName /*"transaction-event-sink"*/);
    }

    
    public ProcessorContext getContext() {
        return context;
    }

    public KafkaStreamsContext initContext(ProcessorContext context) {
        this.context = context;
        return this;
    }

    public String getDomainCommandChannel() {
        return domainCommandChannel;
    }


    public KafkaStreamsContext setDomainCommandChannel(String domainCommandChannelName) {
        this.domainCommandChannel = domainCommandChannelName;
        return this;
    }


    public String getDomainEventChannel() {
        return domainEventChannel;
    }


    public KafkaStreamsContext setDomainEventChannel(String domainEventChannelName) {
        this.domainEventChannel = domainEventChannelName;
        return this;
    }


    public String getTransactionCommandChannel() {
        return transactionCommandChannel;
    }


    public KafkaStreamsContext setTransactionCommandChannel(String transactionCommandChannelName) {
        this.transactionCommandChannel = transactionCommandChannelName;
        return this;
    }

    public String getTransactionEventChannel() {
        return transactionEventChannel;
    }


    public KafkaStreamsContext setTransactionEventChannel(String transactionEventChannelName) {
        this.transactionEventChannel = transactionEventChannelName;
        return this;
    }
}
