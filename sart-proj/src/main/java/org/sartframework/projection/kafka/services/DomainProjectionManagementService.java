package org.sartframework.projection.kafka.services;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Produced;
import org.sartframework.event.DomainEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.SartSerdes;
import org.sartframework.projection.kafka.query.KafkaDomainProjection;
import org.sartframework.query.DomainQuery;
import org.sartframework.service.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainProjectionManagementService<T> implements ManagedService<DomainProjectionManagementService<?>> {

    final static Logger LOGGER = LoggerFactory.getLogger(DomainProjectionManagementService.class);

    final KafkaDomainProjection<?,?> projection;

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    KafkaStreams kafkaStreams;

    public DomainProjectionManagementService(KafkaDomainProjection<?,?> projection, SartKafkaConfiguration kafkaStreamsConfiguration) {
        super();
        this.projection = projection;
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
    }

    @Override
    public DomainProjectionManagementService<T> start() {

        LOGGER.info("Starting domain projection service for projection={}", projection.getName());

        StreamsBuilder builder = new StreamsBuilder();

        builder.stream(projection.getEventTopic(), Consumed.<String, DomainEvent<?>> with(Serdes.String(), SartSerdes.Proto()))

            .filter((agregateKey, event) -> {

                boolean hasEventType = projection.hasEventType(event.getClass());

                LOGGER.info("Filtering domain event projection={}, agregateKey={}, event={}, filter={}", projection.getName(), agregateKey, event,
                    hasEventType);

                return hasEventType;
            })

            .foreach((agregateKey, event) -> {

                LOGGER.info("Domain projection handling event projection={}, event={}", projection.getName(), event);

                projection.handle(event);
            });

        builder.stream(projection.getQueryTopic(), Consumed.<String, DomainQuery> with(Serdes.String(), SartSerdes.Proto()))
        
            .filter((queryKey, query) -> {

                boolean hasQueryType = projection.hasQueryType(query.getClass());

                LOGGER.info("Filtering dmain query projection={}, query={}, event={}, filter={}", projection.getName(), query, hasQueryType);

                return hasQueryType;

            })

            .flatMapValues((query) -> {

                LOGGER.info("Domain projection handling query projection={}, query={}", projection.getName(), query);

                return projection.handleQuery(query);

            })

            .to(projection.getQueryResultTopic(), Produced.with(Serdes.String(), SartSerdes.Proto()));

        Topology projectionTopology = builder.build();

        kafkaStreams = new KafkaStreams(projectionTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("projection-service-" + projection.getName())));

        kafkaStreams.start();

        return this;
    }

    @Override
    public DomainProjectionManagementService<T> stop() {

        LOGGER.info("Stopping projection service for projection={}", projection.getName());

        kafkaStreams.close();

        return this;

    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }

}
