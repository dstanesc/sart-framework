package org.sartframework.demo.cae.client;

import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.projection.ProjectionConfiguration;

public class InputDeckProjectionConfiguration implements ProjectionConfiguration {

    private final SartKafkaConfiguration kafkaStreamsConfiguration;
    
    public InputDeckProjectionConfiguration(SartKafkaConfiguration kafkaStreamsConfiguration) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
    }

    @Override
    public String getEventTopic() {

        return kafkaStreamsConfiguration.getDomainEventTopic();
    }

    @Override
    public String getQueryTopic() {

        return kafkaStreamsConfiguration.getDomainQueryTopic();
    }

    @Override
    public String getQueryResultTopic() {

        return kafkaStreamsConfiguration.getDomainQueryResultTopic();
    }

    @Override
    public String getQueryEventTopic() {

        return kafkaStreamsConfiguration.getDomainQueryEventTopic();
    }

    @Override
    public String getResultListenerServiceName() {

        return "inputDeck-result-listener-client-one";
    }

}
