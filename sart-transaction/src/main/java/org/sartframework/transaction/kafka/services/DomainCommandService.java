package org.sartframework.transaction.kafka.services;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.streams.state.StoreBuilder;
import org.apache.kafka.streams.state.Stores;
import org.sartframework.aggregate.AnnotatedDomainAggregate;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.BusinessTransactionManager;
import org.sartframework.transaction.kafka.processors.DomainCommandProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DomainCommandService implements ManagedService<DomainCommandService> {

    final static Logger LOGGER = LoggerFactory.getLogger(DomainCommandService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final BusinessTransactionManager businessTransactionManager;

    KafkaStreams kafkaStreams;

    @Autowired
    public DomainCommandService(SartKafkaConfiguration kafkaStreamsConfiguration, BusinessTransactionManager businessTransactionManager) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.businessTransactionManager = businessTransactionManager;
    }

    @Override
    public DomainCommandService start() {

        LOGGER.info("Starting domain command service topology");

        final Topology commandHandlingTopology = new Topology();

        StoreBuilder<KeyValueStore<String, AnnotatedDomainAggregate>> aggregateStoreBuilder = Stores
            .<String, AnnotatedDomainAggregate> keyValueStoreBuilder(
                Stores.persistentKeyValueStore(kafkaStreamsConfiguration.getAggregate().getStore().getName()), Serdes.String(), SartSerdes.DomainAggregateSerde());

        commandHandlingTopology
            .addSource("domain-command-source", SartSerdes.String().deserializer(), SartSerdes.DomainCommandSerde().deserializer(),
                kafkaStreamsConfiguration.getDomainCommandTopic())

            .addProcessor("domain-command-validator", () -> new DomainCommandProcessor(kafkaStreamsConfiguration, businessTransactionManager),
                "domain-command-source")

            .addStateStore(aggregateStoreBuilder, "domain-command-validator")

            .addSink("domain-event-sink", kafkaStreamsConfiguration.getDomainEventTopic(), SartSerdes.String().serializer(),
                SartSerdes.DomainEventSerde().serializer(), "domain-command-validator")

            .addSink("transaction-command-sink", kafkaStreamsConfiguration.getTransactionCommandTopic(), SartSerdes.Long().serializer(),
                SartSerdes.TransactionCommandSerde().serializer(), "domain-command-validator")

            .addSink("transaction-event-sink", kafkaStreamsConfiguration.getTransactionEventTopic(), SartSerdes.Long().serializer(),
                SartSerdes.TransactionEventSerde().serializer(), "domain-command-validator");

        kafkaStreams = new KafkaStreams(commandHandlingTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("domain-command-processor")));

        kafkaStreams.start();

        LOGGER.info("Domain command service topology started");

        return this;
    }

    @Override
    public DomainCommandService stop() {
        kafkaStreams.close();
        return this;
    }

}
