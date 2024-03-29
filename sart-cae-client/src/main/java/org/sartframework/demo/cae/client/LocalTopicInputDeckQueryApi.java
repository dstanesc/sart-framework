package org.sartframework.demo.cae.client;

import org.sartframework.demo.cae.query.InputDeckByIdQuery;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.query.InputDeckByXidQuery;
import org.sartframework.driver.LocalTopicQueryApi;
import org.sartframework.kafka.config.SartKafkaConfiguration;

public class LocalTopicInputDeckQueryApi extends LocalTopicQueryApi {

    public LocalTopicInputDeckQueryApi(SartKafkaConfiguration kafkaStreamsConfiguration) {
        super();

        InputDeckProjectionConfiguration projection = new InputDeckProjectionConfiguration(kafkaStreamsConfiguration);

        registerQuerySupport(InputDeckByXidQuery.class, projection);
        registerQuerySupport(InputDeckByIdQuery.class, projection);
        registerQuerySupport(InputDeckByNameQuery.class, projection);
    }

}
