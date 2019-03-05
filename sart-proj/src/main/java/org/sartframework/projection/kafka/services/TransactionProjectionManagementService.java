package org.sartframework.projection.kafka.services;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Produced;
import org.sartframework.event.QueryEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.query.QueryUnsubscribedEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.SartSerdes;
import org.sartframework.projection.kafka.query.KafkaTransactionProjection;
import org.sartframework.query.DomainQuery;
import org.sartframework.service.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionProjectionManagementService<T> implements ManagedService<TransactionProjectionManagementService<?>> {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionProjectionManagementService.class);

    final KafkaTransactionProjection transactionProjection;

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    KafkaStreams kafkaStreams;

    public TransactionProjectionManagementService(KafkaTransactionProjection projection, SartKafkaConfiguration kafkaStreamsConfiguration) {
        super();
        this.transactionProjection = projection;
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
    }

    @Override
    public TransactionProjectionManagementService<T> start() {

        LOGGER.info("Starting transaction projection service for projection={}", transactionProjection.getName());

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(transactionProjection.getEventTopic(), Consumed.<Long, TransactionEvent> with(Serdes.Long(), SartSerdes.Proto()))

            .filter((xid, event) -> {

                boolean hasEventType = transactionProjection.hasEventType(event.getClass());

                LOGGER.info("Filtering transaction event projection={}, xid={}, event={}, filter={}", transactionProjection.getName(), xid, event,
                    hasEventType);

                return hasEventType;
            })

            .foreach((xid, event) -> {

                LOGGER.info("Transaction projection handling event projection={}, event={}", transactionProjection.getName(), event);

                transactionProjection.handle(event);
            });

        builder.stream(transactionProjection.getQueryTopic(), Consumed.<String, DomainQuery> with(Serdes.String(), SartSerdes.Proto()))

            .filter((queryKey, query) -> {

                boolean hasQueryType = transactionProjection.hasQueryType(query.getClass());

                LOGGER.info("Filtering transaction query projection={}, query={}, event={}, filter={}", transactionProjection.getName(), query,
                    hasQueryType);

                return hasQueryType;

            })

            .flatMapValues((query) -> {

                LOGGER.info("Transaction projection handling query projection={}, query={}", transactionProjection.getName(), query);

                return transactionProjection.handleQuery(query);

            })

            .to(transactionProjection.getQueryResultTopic(), Produced.<String, Object> with(Serdes.String(), SartSerdes.Proto()));
        
        
        builder.stream(transactionProjection.getQueryEventTopic(), Consumed.<String, QueryEvent> with(Serdes.String(), SartSerdes.Proto()))
        
            .foreach((queryKey, queryEvent) -> {
                if (queryEvent instanceof QueryUnsubscribedEvent) {
                    transactionProjection.unsubscribe(queryKey);
                }
            });

        Topology projectionTopology = builder.build();

        kafkaStreams = new KafkaStreams(projectionTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("projection-service-" + transactionProjection.getName())));

        kafkaStreams.start();

        return this;
    }

    @Override
    public TransactionProjectionManagementService<T> stop() {

        LOGGER.info("Stopping projection service for projection={}", transactionProjection.getName());

        kafkaStreams.close();

        return this;

    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }

}
