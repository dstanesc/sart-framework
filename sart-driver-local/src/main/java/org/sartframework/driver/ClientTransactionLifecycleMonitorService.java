package org.sartframework.driver;

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
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.TransactionMonitors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClientTransactionLifecycleMonitorService implements ManagedService<ClientTransactionLifecycleMonitorService> {

    final static Logger LOGGER = LoggerFactory.getLogger(ClientTransactionLifecycleMonitorService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final TransactionMonitors transactionMonitors = new TransactionMonitors();
    
    final Long xid;
    
    KafkaStreams kafkaStreams;

    ExecutorService executor = Executors.newFixedThreadPool(2);
    

    public ClientTransactionLifecycleMonitorService(SartKafkaConfiguration kafkaStreamsConfiguration, Long xid) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.xid = xid;
    }

    @Override
    public ClientTransactionLifecycleMonitorService start() {

        LOGGER.info("Starting transaction lifecycle monitor");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, TransactionEvent> transactionEventStream = builder.stream(kafkaStreamsConfiguration.getTransactionEventTopic(),
            Consumed.<Long, TransactionEvent> with(Serdes.Long(), SartSerdes.TransactionEventSerde()));

        transactionEventStream.foreach((xid, event) -> {

                executor.submit(() -> dispatchEvent(xid, event));
            
        });

        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("client-one-transaction-lifecycle-monitor")));

        kafkaStreams.start();

        return this;
    }

    protected void dispatchEvent(Long xid, TransactionEvent event) {
 
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
            transactionMonitors.onNextLogged((ProgressLoggedEvent) event);
        }
    }


    @Override
    public ClientTransactionLifecycleMonitorService stop() {

        LOGGER.info("Stopping transaction lifecycle monitor");

        kafkaStreams.close();
        
      //  transactionMonitors.close();
         
        return this;
    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }

    public TransactionMonitors getTransactionMonitors() {
        return transactionMonitors;
    }

}
