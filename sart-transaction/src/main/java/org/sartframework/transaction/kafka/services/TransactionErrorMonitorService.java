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
import org.sartframework.error.transaction.TransactionError;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.TransactionErrorMonitors;
import org.sartframework.transaction.kafka.KafkaBusinessTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionErrorMonitorService implements ManagedService<TransactionErrorMonitorService> {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionErrorMonitorService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final KafkaBusinessTransactionManager businessTransactionManager;

    KafkaStreams kafkaStreams;

    Map<Long, TransactionErrorMonitors> subscribedMonitors = new HashMap<>();

    ExecutorService executor = Executors.newFixedThreadPool(1);

    @Autowired
    public TransactionErrorMonitorService(SartKafkaConfiguration kafkaStreamsConfiguration,
                                          KafkaBusinessTransactionManager businessTransactionManager) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.businessTransactionManager = businessTransactionManager;
    }

    @Override
    public TransactionErrorMonitorService start() {

        LOGGER.info("Starting transaction error monitor service");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, TransactionError> transactionErrorStream = builder.stream(kafkaStreamsConfiguration.getTransactionErrorTopic(),
            Consumed.<Long, TransactionError> with(Serdes.Long(), SartSerdes.TransactionErrorSerde()));

        transactionErrorStream.foreach((xid, transactionError) -> {
            
            businessTransactionManager.abortTransaction(xid);
            
            TransactionErrorMonitors transactionErrorMonitors = getSubscribedMonitors(xid);
            
            if (transactionErrorMonitors != null) {
                
                executor.submit(() -> dispatchError(transactionErrorMonitors, transactionError));
            }
        });

        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("transaction-error-monitor")));

        kafkaStreams.start();

        businessTransactionManager.registerTransactionErrorMonitorService(this);
        
        return this;
    }

    private void dispatchError(TransactionErrorMonitors transactionErrorMonitors, TransactionError transactionError) {

        transactionErrorMonitors.onNextTransactionError(transactionError);
    }


    public synchronized TransactionErrorMonitors getSubscribedMonitors(long xid) {
        
        TransactionErrorMonitors transactionErrorMonitors = subscribedMonitors.get(xid);
        
        return transactionErrorMonitors;
    }
    
    public void registerMonitors(long xid) {

        TransactionErrorMonitors transactionErrorMonitors = new TransactionErrorMonitors(xid);

        subscribedMonitors.put(xid, transactionErrorMonitors);
    }

    public synchronized TransactionErrorMonitors unregisterMonitors(long xid) {

        LOGGER.info("Unregister subscribed transaction error monitors for xid={}", xid);

        TransactionErrorMonitors transactionErrorMonitors = subscribedMonitors.remove(xid);

        //transactionErrorMonitors.close();

        return transactionErrorMonitors;
    }

    @Override
    public TransactionErrorMonitorService stop() {

        LOGGER.info("Stopping transaction error monitor service");

        kafkaStreams.close();

        return this;
    }

}
