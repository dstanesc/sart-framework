package org.sartframework.demo.cae.projection.config;

import org.sartframework.demo.cae.projection.InputDeckProjection;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.projection.kafka.query.KafkaDomainQueryManager;
import org.sartframework.projection.kafka.services.DomainProjectionManagementService;
import org.sartframework.query.QueryManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ProjectionConfig {

    @Bean
    public QueryManager buildTransactionQueryManager(SartKafkaConfiguration kafkaConfiguration, InputDeckProjection inputDeckProjection) {
       return new KafkaDomainQueryManager(kafkaConfiguration, inputDeckProjection);
    }
    
    @Bean
    public DomainProjectionManagementService<?> domainProjectionManagementService(SartKafkaConfiguration kafkaConfiguration,  InputDeckProjection inputDeckProjection) {
        return new DomainProjectionManagementService<>(inputDeckProjection, kafkaConfiguration);
    }
}
