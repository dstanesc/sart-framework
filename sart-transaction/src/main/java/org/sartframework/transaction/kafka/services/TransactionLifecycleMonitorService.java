package org.sartframework.transaction.kafka.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.ProgressLoggedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.TransactionMonitors;
import org.sartframework.transaction.kafka.KafkaBusinessTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionLifecycleMonitorService implements ManagedService<TransactionLifecycleMonitorService> {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionLifecycleMonitorService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final KafkaBusinessTransactionManager businessTransactionManager;

    KafkaStreams kafkaStreams;

    Map<Long, TransactionMonitors> subscribedMonitors = new HashMap<>();
    
    ExecutorService executor = Executors.newFixedThreadPool(5);
    

    @Autowired
    public TransactionLifecycleMonitorService(SartKafkaConfiguration kafkaStreamsConfiguration,
                                              KafkaBusinessTransactionManager businessTransactionManager) {
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.businessTransactionManager = businessTransactionManager;
    }

    @Override
    public TransactionLifecycleMonitorService start() {

        LOGGER.info("Starting transaction lifecycle monitor");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, TransactionEvent> transactionEventStream = builder.stream(kafkaStreamsConfiguration.getTransactionEventTopic(),
            Consumed.<Long, TransactionEvent> with(Serdes.Long(), SartSerdes.Proto()));

        transactionEventStream.foreach((xid, event) -> {

            if (hasSubscribedMonitors(xid)) {
                //dispatchEvent(xid, event);
                executor.submit(() -> dispatchEvent(xid, event));
            }
        });

        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("transaction-lifecycle-monitor")));

        kafkaStreams.start();

        businessTransactionManager.registerTransactionLifecycleMonitorService(this);

        return this;
    }

    protected void dispatchEvent(Long xid, TransactionEvent event) {
        TransactionMonitors transactionMonitors = getSubscribedMonitors(xid);

        if (event instanceof TransactionStartedEvent) {
            transactionMonitors.onNextStart((TransactionStartedEvent) event);
        } else if (event instanceof TransactionCommittedEvent) {
            transactionMonitors.onNextCommit((TransactionCommittedEvent) event);
        }else if (event instanceof TransactionCompletedEvent) {
            transactionMonitors.onNextComplete((TransactionCompletedEvent) event);
        } else if (event instanceof TransactionAbortedEvent) {
            transactionMonitors.onNextAbort((TransactionAbortedEvent) event);
        } else if (event instanceof ConflictResolvedEvent) {
            transactionMonitors.onNextConflict((ConflictResolvedEvent) event);
        } else if (event instanceof ProgressLoggedEvent) {

            ProgressLoggedEvent progressEvent = (ProgressLoggedEvent) event;

            long xcs = progressEvent.getXcs();

            if (xcs < 0) {
                transactionMonitors.onNextCompensate(progressEvent.getDomainEvent());
            } else if (xcs > 0) {
                transactionMonitors.onNextProgress(progressEvent.getDomainEvent());
            } else
                throw new UnsupportedOperationException(" Invalid xcs " + xcs);
        }
    }

    public synchronized boolean hasSubscribedMonitors(long xid) {

        return subscribedMonitors.containsKey(xid);
    }

    public synchronized TransactionMonitors getSubscribedMonitors(long xid) {
        TransactionMonitors transactionMonitors = subscribedMonitors.get(xid);
        if (transactionMonitors == null) {
            transactionMonitors = new TransactionMonitors();
            subscribedMonitors.put(xid, transactionMonitors);
        }

        return transactionMonitors;
    }
    
    public synchronized TransactionMonitors unregisterSubscribedMonitors(long xid) {
        
        LOGGER.info("Unregister subscribed monitors for xid={}", xid);
        
        TransactionMonitors  transactionMonitors = subscribedMonitors.remove(xid);
        
        transactionMonitors.close();
        
        return transactionMonitors;
    }

    @Override
    public TransactionLifecycleMonitorService stop() {

        LOGGER.info("Stopping transaction lifecycle monitor");

        kafkaStreams.close();

        return this;
    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }

}
