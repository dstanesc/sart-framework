package org.sartframework.config;

import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.projection.ConflictResolutionProjection;
import org.sartframework.projection.kafka.query.KafkaTransactionQueryManager;
import org.sartframework.projection.kafka.services.TransactionProjectionManagementService;
import org.sartframework.query.QueryManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ConflictConfig {

    @Bean
    public QueryManager buildTransactionQueryManager(SartKafkaConfiguration kafkaConfiguration, ConflictResolutionProjection conflictResolutionProjection) {
       return new KafkaTransactionQueryManager(kafkaConfiguration, conflictResolutionProjection);
    }
    
    @Bean
    public TransactionProjectionManagementService<?> domainProjectionManagementService(SartKafkaConfiguration kafkaConfiguration, ConflictResolutionProjection conflictResolutionProjection) {
        return new TransactionProjectionManagementService<>(conflictResolutionProjection, kafkaConfiguration);
    }
}
