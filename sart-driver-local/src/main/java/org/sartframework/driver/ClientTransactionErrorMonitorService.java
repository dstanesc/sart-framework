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
import org.sartframework.error.transaction.TransactionError;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.TransactionErrorMonitors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTransactionErrorMonitorService implements ManagedService<ClientTransactionErrorMonitorService> {

    final static Logger LOGGER = LoggerFactory.getLogger(ClientTransactionErrorMonitorService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final TransactionErrorMonitors transactionErrorMonitors;

    KafkaStreams kafkaStreams;

    ExecutorService executor = Executors.newFixedThreadPool(1);

    public ClientTransactionErrorMonitorService(SartKafkaConfiguration kafkaStreamsConfiguration, Long xid) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.transactionErrorMonitors = new TransactionErrorMonitors(xid);
    }

    @Override
    public ClientTransactionErrorMonitorService start() {

        LOGGER.info("Starting transaction error monitor");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, TransactionError> transactionErrorStream = builder.stream(kafkaStreamsConfiguration.getTransactionErrorTopic(),
            Consumed.<Long, TransactionError> with(Serdes.Long(), SartSerdes.TransactionErrorSerde()));

        transactionErrorStream

            .filter((xid, error) -> transactionErrorMonitors.getXid() == xid)

            .foreach((xid, error) -> {

                executor.submit(() -> dispatchError(error));
            });

        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("transaction-error-monitor")));

        kafkaStreams.start();

        return this;
    }

    private void dispatchError(TransactionError transactionError) {
        
        transactionErrorMonitors.onNextTransactionError(transactionError);
    }

    @Override
    public ClientTransactionErrorMonitorService stop() {

        LOGGER.info("Stopping transaction error monitor");

        kafkaStreams.close();

        return this;
    }

    public TransactionErrorMonitors getTransactionErrorMonitors() {
        return transactionErrorMonitors;
    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }
}
