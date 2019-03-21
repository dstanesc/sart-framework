package org.sartframework.projection.kafka.services;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.projection.ProjectionConfiguration;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.EmptyResult;
import org.sartframework.result.EndResult;
import org.sartframework.result.QueryResult;
import org.sartframework.service.ManagedService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;
import reactor.core.publisher.ReplayProcessor;

public class QueryResultListenerService<T> implements ManagedService<QueryResultListenerService<?>> {

    final static Logger LOGGER = LoggerFactory.getLogger(QueryResultListenerService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final ProjectionConfiguration projectionConfiguration;

    final DomainQuery domainQuery;

    final ReplayProcessor<T> resultPublisher = ReplayProcessor.<T> create();

    KafkaStreams kafkaStreams;

    public QueryResultListenerService(SartKafkaConfiguration kafkaStreamsConfiguration, ProjectionConfiguration projectionConfiguration, DomainQuery domainQuery) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.projectionConfiguration = projectionConfiguration;
        this.domainQuery = domainQuery;
    }

    public DomainQuery getDomainQuery() {
        return domainQuery;
    }

    @SuppressWarnings("unchecked")
    @Override
    public QueryResultListenerService<T> start() {

        LOGGER.info("Strting result listener for queryKey={}", getDomainQuery().getQueryKey());

        StreamsBuilder builder = new StreamsBuilder();

        builder

            .stream(projectionConfiguration.getQueryResultTopic(), Consumed.<String, QueryResult> with(Serdes.String(), SartSerdes.QueryResultSerde()))

            .filter((resultKey, result) -> (resultKey.equals(getDomainQuery().getQueryKey())))

            .foreach((resultKey, result) -> {

                LOGGER.info("Result message incoming resultKey={}, result={}", resultKey, result);

                if (result instanceof EmptyResult) {

                    LOGGER.info("Result stream onComplete invoked");

                    resultPublisher.onComplete();

                } else if (result instanceof EndResult) {

                    LOGGER.info("Result stream onComplete invoked");

                    resultPublisher.onComplete();

                } else {

                    LOGGER.info("Result stream publishes to resultPublisher resultKey={}", resultKey);

                    resultPublisher.onNext((T) result);
                }
            });

        Topology listenerTopology = builder.build();

        kafkaStreams = new KafkaStreams(listenerTopology, new StreamsConfig(
            kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig(projectionConfiguration.getResultListenerServiceName() + getDomainQuery().getQueryKey())));

        kafkaStreams.start();

        return this;
    }

    @Override
    public QueryResultListenerService<T> stop() {

        LOGGER.info("Stopping result listener for queryKey={}", getDomainQuery().getQueryKey());

        kafkaStreams.close();

        return this;

    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }

    public Flux<T> getResultPublisher() {
        return resultPublisher.doOnEach(result -> {
            LOGGER.info("Result yield by result listener service {}", result);
        });
    }

}
