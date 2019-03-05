package org.sartframework.transaction.kafka.services;

import java.util.SortedSet;
import java.util.TreeSet;

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
import org.sartframework.kafka.serializers.SartSerdes;
import org.sartframework.service.ManagedService;
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

    static class TxnSet {
        
        TransactionEvent lastEvent;
        
        Long highestCommited = -1L;

        SortedSet<Long> txn;

        public TxnSet() {
            super();
            txn = new TreeSet<>();
        }

        public void add(Long xid) {
            txn.add(xid);
        }

        public void remove(Long xid) {
            txn.remove(xid);
        }

        public SortedSet<Long> getTxn() {
            return txn;
        }
        
        public TransactionEvent getLastEvent() {
            return lastEvent;
        }

        public void setLastEvent(TransactionEvent lastEvent) {
            this.lastEvent = lastEvent;
        }

        public Long getHighestCommited() {
            return highestCommited;
        }

        public void setHighestCommited(Long higestCommited) {
            this.highestCommited = higestCommited;
        }

        public void updateHighestCommited(Long other) {
            if(other > highestCommited) {
                setHighestCommited(other);
            }
        }
    }

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionSessionMonitorService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final KafkaBusinessTransactionManager businessTransactionManager;

    KafkaStreams kafkaStreams;

    @Autowired
    public TransactionSessionMonitorService(SartKafkaConfiguration kafkaStreamsConfiguration,
                                            KafkaBusinessTransactionManager businessTransactionManager) {
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.businessTransactionManager = businessTransactionManager;
    }

    @Override
    public TransactionSessionMonitorService start() {

        LOGGER.info("Starting session transaction monitor");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, TransactionEvent> transactionEventStream = builder.stream(kafkaStreamsConfiguration.getTransactionEventTopic(),
            Consumed.<Long, TransactionEvent> with(Serdes.Long(), SartSerdes.Proto()));
        
        transactionEventStream

            .filter((xid, event) -> event instanceof TransactionStartedEvent || event instanceof TransactionCommittedEvent
                || event instanceof TransactionAbortedEvent)

            .groupBy((xid, event) -> "groupingConstant", Serialized.<String, TransactionEvent> with(Serdes.String(), SartSerdes.Proto()))

            .aggregate(TxnSet::new, (type, event, txnSet) -> {

                if (event instanceof TransactionStartedEvent) {

                    LOGGER.info("Adding running txn xid={}", event.getXid());
                    
                    txnSet.add(event.getXid());

                } else if (event instanceof TransactionCommittedEvent) {

                    LOGGER.info("Remove running txn xid={}", event.getXid());
                    
                    LOGGER.info("Set highest committed txn xid={}", event.getXid());
                    
                    txnSet.remove(event.getXid());
                    
                    txnSet.setHighestCommited(event.getXid());

                } else if (event instanceof TransactionAbortedEvent) {

                    LOGGER.info("Remove running txn xid={}", event.getXid());
                    
                    txnSet.remove(event.getXid());
                }
                
                txnSet.setLastEvent(event);

                return txnSet;

            }, Materialized.<String, TxnSet, KeyValueStore<Bytes, byte[]>> as(RUNNING_TRANSACTIONS_STORE)
                .withKeySerde(Serdes.String())
                .withValueSerde(SartSerdes.Proto()))
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
            
            .to(kafkaStreamsConfiguration.getTransactionEventTopic(), Produced.<Long, TransactionCompletedEvent>with(Serdes.Long(), SartSerdes.Proto()));

        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("transaction-session-monitor")));

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
        
        transactionSnapshot.setTimestamp(System.currentTimeMillis());

        ReadOnlyKeyValueStore<String, TxnSet> runningTransactionsStore = getKafkaStreams().store(RUNNING_TRANSACTIONS_STORE,
            QueryableStoreTypes.<String, TxnSet> keyValueStore());
        
        runningTransactionsStore.all().forEachRemaining(keyValue -> {
            
            SortedSet<Long> runningTransactions = keyValue.value.getTxn();
            
            Long highestCommited = keyValue.value.getHighestCommited();
            
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
