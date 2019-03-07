package org.sartframework.transaction.kafka;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.AbortTransactionCommand;
import org.sartframework.command.transaction.CommitTransactionCommand;
import org.sartframework.command.transaction.CreateTransactionCommand;
import org.sartframework.command.transaction.StartTransactionCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.kafka.channels.KafkaWriters;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.session.SystemSnapshot;
import org.sartframework.transaction.BusinessTransactionManager;
import org.sartframework.transaction.generator.TransactionSequence;
import org.sartframework.transaction.generator.ZookeeperTransactionSequence;
import org.sartframework.transaction.kafka.services.TransactionCommandService;
import org.sartframework.transaction.kafka.services.TransactionLifecycleMonitorService;
import org.sartframework.transaction.kafka.services.TransactionRollbackService;
import org.sartframework.transaction.kafka.services.TransactionSessionMonitorService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Component
public class KafkaBusinessTransactionManager implements BusinessTransactionManager {

    final static Logger LOGGER = LoggerFactory.getLogger(KafkaBusinessTransactionManager.class);

    private static KafkaBusinessTransactionManager instance;

    final private KafkaWriters writeChannels;

    final private SartKafkaConfiguration kafkaStreamsConfiguration;
    
    final private TransactionSequence transactionSequence;

    private Map<Long, TransactionRollbackService> rolbackServices = new HashMap<>();

    private TransactionCommandService transactionCommandService;
    
    private TransactionLifecycleMonitorService transactionLifecycleMonitorService;
    
    private TransactionSessionMonitorService transactionSessionMonitorService;

    @Autowired
    public KafkaBusinessTransactionManager(KafkaWriters writeChannels, SartKafkaConfiguration kafkaStreamsConfiguration,  ZookeeperTransactionSequence transactionSequence) {
        super();
        this.transactionSequence = transactionSequence;
        this.writeChannels = writeChannels;
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
    }

    @PostConstruct
    public BusinessTransactionManager init() {

        instance = this;

        return this;
    }

    public static KafkaBusinessTransactionManager get() {

        return instance;
    }

    @Override
    public long nextTransaction() {

        long xid = transactionSequence.next();
        
        //should we better have an explicit dependency on creating monitors before start instead of time sequence
        completeListener(xid).subscribe( c -> {
            
            unregisterTransactionMonitors(xid);
        });
        
        createTransaction(xid);

        return xid;
    }

    @Override
    public int status(long xid) {

        ReadOnlyKeyValueStore<Long, KafkaTransactionAggregate> transactionStore = transactionCommandService.getKafkaStreams().store(
            kafkaStreamsConfiguration.getTransaction().getStore().getName(), QueryableStoreTypes.<Long, KafkaTransactionAggregate> keyValueStore());

        return transactionStore.get(xid).getStatus();
    }

    
    @Override
    public SystemSnapshot systemSnapshot(long xid) {

        ReadOnlyKeyValueStore<Long, KafkaTransactionAggregate> transactionStore = transactionCommandService.getKafkaStreams().store(
            kafkaStreamsConfiguration.getTransaction().getStore().getName(), QueryableStoreTypes.<Long, KafkaTransactionAggregate> keyValueStore());

        KafkaTransactionAggregate transactionAggregate = transactionStore.get(xid);
        
        return transactionAggregate.getSystemSnapshot();
    }
    
    public PartitionOffset partitionOffset(long xid) {
        
        ReadOnlyKeyValueStore<Long, KafkaTransactionAggregate> transactionStore = transactionCommandService.getKafkaStreams().store(
            kafkaStreamsConfiguration.getTransaction().getStore().getName(), QueryableStoreTypes.<Long, KafkaTransactionAggregate> keyValueStore());

        KafkaTransactionAggregate transactionAggregate = transactionStore.get(xid);

        return new PartitionOffset(transactionAggregate.getPartition(), transactionAggregate.getOffset());
    }

    
    public void registerTransactionCommandService(TransactionCommandService transactionCommandService) {

        this.transactionCommandService = transactionCommandService;
    }
    
    
    public void registerTransactionLifecycleMonitorService(TransactionLifecycleMonitorService transactionLifecycleMonitorService) {

        this.transactionLifecycleMonitorService = transactionLifecycleMonitorService;
    }

    public void registerTransactionSessionMonitorService(TransactionSessionMonitorService transactionSessionMonitorService) {

        this.transactionSessionMonitorService = transactionSessionMonitorService;
    }
    
    public void registerRollbackService(TransactionRollbackService transactionRollbackService) {

        this.rolbackServices.put(transactionRollbackService.getXid(), transactionRollbackService);
    }

    public void unregisterRollbackService(long xid) {

        TransactionRollbackService transactionRollbackService = rolbackServices.remove(xid);
        transactionRollbackService.stop();
    }
    
    public void unregisterTransactionMonitors(long xid) {
       
       this.transactionLifecycleMonitorService.unregisterSubscribedMonitors(xid);
    }

    @Override
    public void createTransaction(long xid) {

        LOGGER.info("createTransaction {}", xid);

        CreateTransactionCommand createTransactionCommand = new CreateTransactionCommand(xid);

        writeChannels.getTransactionCommandWriter().sendDefault(xid, createTransactionCommand);
    }

    @Override
    public void startTransaction(long xid, int isolation) {

        LOGGER.info("startTransaction {}, isolation {} ", xid, isolation);
        
        SystemSnapshot systemSnapshot = transactionSessionMonitorService.systemSnapshot();
        
        LOGGER.info("Taking system snapshot {} ", systemSnapshot);
        
        StartTransactionCommand startTransactionCommand = new StartTransactionCommand(xid, isolation, systemSnapshot);

        writeChannels.getTransactionCommandWriter().sendDefault(xid, startTransactionCommand);
    }
    

    @Override
    public void commitTransaction(long xid, long xct) {

        LOGGER.info("commitTransaction xid={} xct={}", xid, xct);

        CommitTransactionCommand commitTransactionCommand = new CommitTransactionCommand(xid, xct);

        writeChannels.getTransactionCommandWriter().sendDefault(xid, commitTransactionCommand);
    }

    @Override
    public void abortTransaction(long xid) {

        AbortTransactionCommand abortTransactionCommand = new AbortTransactionCommand(xid);

        writeChannels.getTransactionCommandWriter().sendDefault(xid, abortTransactionCommand);

        LOGGER.info("abortTransaction {} {}", xid, abortTransactionCommand);

    }

    @Override
    public void publish(DomainCommand domainCommand) {

        LOGGER.info("publish domain command {} {}, xid={}", domainCommand.getAggregateKey(), domainCommand, domainCommand.getXid());

        writeChannels.getDomainCommandWriter().sendDefault(domainCommand.getAggregateKey(), domainCommand);
    }

    @Override
    public void publish(DomainEvent<? extends DomainCommand> domainEvent) {

        LOGGER.info("publish domain event {} {}, xid={}", domainEvent.getAggregateKey(), domainEvent, domainEvent.getXid());

        writeChannels.getDomainEventWriter().sendDefault(domainEvent.getAggregateKey(), domainEvent);
    }

    @Override
    public void publish(TransactionCommand transactionCommand) {

        LOGGER.info("publish transaction command xid={} {}", transactionCommand.getXid(), transactionCommand);

        writeChannels.getTransactionCommandWriter().sendDefault(transactionCommand.getXid(), transactionCommand);
    }

    @Override
    public void publish(TransactionEvent transactionEvent) {

        LOGGER.info("publish transaction event xid={} {}", transactionEvent.getXid(), transactionEvent);

        writeChannels.getTransactionEventWriter().sendDefault(transactionEvent.getXid(), transactionEvent);
    }
    
//    public TransactionMonitors createTransactionMonitors(long xid) {   
//        return  transactionLifecycleMonitorService.getSubscribedMonitors(xid);
//    }

    @Override
    public Flux<DomainEvent<? extends DomainCommand>> transactionProgressEvents(long xid) {
       return transactionLifecycleMonitorService.getSubscribedMonitors(xid).progressMonitor();
    }

    @Override
    public Flux<DomainEvent<? extends DomainCommand>> transactionCompensationEvents(long xid) {
        return transactionLifecycleMonitorService.getSubscribedMonitors(xid).compensateMonitor();
    }

    @Override
    public Mono<TransactionCompletedEvent> completeListener(long xid) {
        return transactionLifecycleMonitorService.getSubscribedMonitors(xid).completeMonitor();
    }

    @Override
    public Mono<TransactionCommittedEvent> commitListener(long xid) {    
        return transactionLifecycleMonitorService.getSubscribedMonitors(xid).commitMonitor();
    }

    @Override
    public Mono<TransactionAbortedEvent> abortListener(long xid) {
        return transactionLifecycleMonitorService.getSubscribedMonitors(xid).abortMonitor();
    }

    @Override
    public Mono<TransactionStartedEvent> startListener(long xid) {
       return transactionLifecycleMonitorService.getSubscribedMonitors(xid).startMonitor();
    }

    @Override
    public Flux<ConflictResolvedEvent> conflictListener(long xid) {
        return transactionLifecycleMonitorService.getSubscribedMonitors(xid).conflictResolvedMonitor();
    }

    public SartKafkaConfiguration getKafkaStreamsConfiguration() {
        return kafkaStreamsConfiguration;
    }

}
