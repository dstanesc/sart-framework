package org.sartframework.transaction.kafka.services;

import java.util.SortedSet;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.common.utils.Bytes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.apache.kafka.streams.kstream.Materialized;
import org.apache.kafka.streams.kstream.Produced;
import org.apache.kafka.streams.kstream.Serialized;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.session.RunningTransactions;
import org.sartframework.session.SystemSnapshot;
import org.sartframework.transaction.BusinessTransactionManager;
import org.sartframework.transaction.kafka.KafkaBusinessTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionSessionMonitorService implements ManagedService<TransactionSessionMonitorService> {

    private static final String RUNNING_TRANSACTIONS_STORE = "runningTransactionsStore";

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionSessionMonitorService.class);

    final SartKafkaConfiguration sartKafkaConfiguration;

    final KafkaBusinessTransactionManager businessTransactionManager;

    KafkaStreams kafkaStreams;

    @Autowired
    public TransactionSessionMonitorService(SartKafkaConfiguration sartKafkaConfiguration,
                                            KafkaBusinessTransactionManager businessTransactionManager) {
        this.sartKafkaConfiguration = sartKafkaConfiguration;
        this.businessTransactionManager = businessTransactionManager;
    }

    @Override
    public TransactionSessionMonitorService start() {

        LOGGER.info("Starting session transaction monitor");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, TransactionEvent> transactionEventStream = builder.stream(sartKafkaConfiguration.getTransactionEventTopic(),
            Consumed.<Long, TransactionEvent> with(Serdes.Long(), SartSerdes.TransactionEventSerde()));
        
        transactionEventStream

            .filter((xid, event) -> event instanceof TransactionStartedEvent || event instanceof TransactionCommittedEvent
                || event instanceof TransactionAbortedEvent)

            .groupBy((xid, event) -> "groupingConstant", Serialized.<String, TransactionEvent> with(Serdes.String(), SartSerdes.TransactionEventSerde()))

            .aggregate(RunningTransactions::new, (type, event, running) -> {

                if (event instanceof TransactionStartedEvent) {

                    LOGGER.info("Adding running txn xid={}", event.getXid());
                    
                    running.add(event.getXid());

                } else if (event instanceof TransactionCommittedEvent) {

                    LOGGER.info("Remove running txn xid={}", event.getXid());
                    
                    LOGGER.info("Set highest committed txn xid={}", event.getXid());
                    
                    running.remove(event.getXid());
                    
                    running.setHighestCommitted(event.getXid());

                } else if (event instanceof TransactionAbortedEvent) {

                    LOGGER.info("Remove running txn xid={}", event.getXid());
                    
                    running.remove(event.getXid());
                }
                
                running.setLastEvent(event);

                return running;

            }, Materialized.<String, RunningTransactions, KeyValueStore<Bytes, byte[]>> as(sartKafkaConfiguration.getSystemPrefixedName(RUNNING_TRANSACTIONS_STORE))
                .withKeySerde(Serdes.String())
                .withValueSerde(SartSerdes.RunningTransactionsSerde()))
               // .withCachingEnabled())
            
            .toStream()
            
            .filter((xid, txn) -> txn.getLastEvent() instanceof TransactionCommittedEvent || txn.getLastEvent() instanceof TransactionAbortedEvent)
            
            .map((constant, txn) -> {
                
                TransactionEvent completeEvent = txn.getLastEvent();
                
                long xid = completeEvent.getXid();
                
                int status;
                
                if( completeEvent instanceof TransactionCommittedEvent) {
                    
                    status = BusinessTransactionManager.STATUS_COMMITTED;
                    
                } else if ( completeEvent instanceof TransactionAbortedEvent) {
                    
                    status = BusinessTransactionManager.STATUS_ABORTED;
                    
                } else throw new UnsupportedOperationException("Unsupported transaction completion " + completeEvent);
                
                LOGGER.info("Dispatching TransactionCompletedEvent txn xid={}", xid);
                
                return KeyValue.<Long, TransactionCompletedEvent> pair(xid, new TransactionCompletedEvent(xid, status));
            })
            
            .to(sartKafkaConfiguration.getTransactionEventTopic(), Produced.<Long, TransactionCompletedEvent>with(Serdes.Long(), SartSerdes.TransactionEventSerde()));

        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(sartKafkaConfiguration.getKafkaStreamsProcessorConfig("transaction-session-monitor")));

        kafkaStreams.start();

        businessTransactionManager.registerTransactionSessionMonitorService(this);

        return this;
    }

    @Override
    public TransactionSessionMonitorService stop() {

        LOGGER.info("Stopping transaction lifecycle monitor");

        kafkaStreams.close();

        return this;
    }

    public SystemSnapshot systemSnapshot() {

        SystemSnapshot transactionSnapshot = new SystemSnapshot();
        
        transactionSnapshot.setSid(sartKafkaConfiguration.getSid());
        transactionSnapshot.setTimestamp(System.currentTimeMillis());

        ReadOnlyKeyValueStore<String, RunningTransactions> runningTransactionsStore = getKafkaStreams().store(sartKafkaConfiguration.getSystemPrefixedName(RUNNING_TRANSACTIONS_STORE),
            QueryableStoreTypes.<String, RunningTransactions> keyValueStore());
        
        runningTransactionsStore.all().forEachRemaining(keyValue -> {
            
            SortedSet<Long> runningTransactions = keyValue.value.getTxn();
            
            Long highestCommited = keyValue.value.getHighestCommitted();
            
            LOGGER.info("systemSnapshot highestCommited={}", highestCommited);
            
            LOGGER.info("systemSnapshot running={}", runningTransactions);

            transactionSnapshot.setRunning(runningTransactions);
            transactionSnapshot.setHighestCommitted(highestCommited);
        });
        
        return transactionSnapshot;
    }
    
    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }

    
}
