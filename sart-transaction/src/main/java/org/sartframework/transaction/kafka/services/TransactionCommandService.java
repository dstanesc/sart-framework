package org.sartframework.transaction.kafka.services;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.kafka.KafkaBusinessTransactionManager;
import org.sartframework.transaction.kafka.KafkaTransactionAggregate;
import org.sartframework.transaction.kafka.processors.TransactionCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TransactionCommandService implements ManagedService<TransactionCommandService> {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionCommandService.class);
    
    final SartKafkaConfiguration kafkaStreamsConfiguration;
    
    final KafkaBusinessTransactionManager businessTransactionManager;

    KafkaStreams kafkaStreams;

    @Autowired
    public TransactionCommandService(SartKafkaConfiguration kafkaStreamsConfiguration, KafkaBusinessTransactionManager businessTransactionManager) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.businessTransactionManager = businessTransactionManager;
    }

    @Override
    public TransactionCommandService start() {

        LOGGER.info("Starting transaction command service topology");
        
        final Topology commandHandlingTopology = new Topology();

        StoreBuilder<KeyValueStore<Long, KafkaTransactionAggregate>> aggregateStoreBuilder = Stores.<Long, KafkaTransactionAggregate> keyValueStoreBuilder(
            Stores.persistentKeyValueStore(kafkaStreamsConfiguration.getTransactionStoreName()), Serdes.Long(), SartSerdes.TransactionAggregateSerde());

        commandHandlingTopology
            .addSource("transaction-command-source", SartSerdes.Long().deserializer(), SartSerdes.TransactionCommandSerde().deserializer(),
                kafkaStreamsConfiguration.getTransactionCommandTopic())

            .addProcessor("transaction-command-validator", () -> new TransactionCommandProcessor(kafkaStreamsConfiguration, businessTransactionManager),
                "transaction-command-source")

            .addStateStore(aggregateStoreBuilder, "transaction-command-validator");
//FIXME, should be used instead see  TransactionCommandProcessor      
/*
            .addSink("transaction-command-sink", kafkaStreamsConfiguration.getTransactionCommandTopic(), SartSerdes.Long().serializer(),
                SartSerdes.TransactionCommandSerde().serializer(), "transaction-command-validator")

            .addSink("domain-command-sink", kafkaStreamsConfiguration.getDomainCommandTopic(), SartSerdes.String().serializer(),
                SartSerdes.DomainCommandSerde().serializer(), "transaction-command-validator");

*/
        kafkaStreams = new KafkaStreams(commandHandlingTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("transaction-command-processor")));

        kafkaStreams.start();

        LOGGER.info("Transaction command service topology started");
        
        businessTransactionManager.registerTransactionCommandService(this);
        
        return this;
    }

    @Override
    public TransactionCommandService stop() {
        kafkaStreams.close();
        return this;
    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }
}
