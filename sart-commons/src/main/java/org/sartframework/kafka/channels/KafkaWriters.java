package org.sartframework.kafka.channels;

import org.apache.kafka.clients.admin.NewTopic;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.QueryEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.QueryResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class KafkaWriters {

    final SartKafkaConfiguration sartKafkaConfiguration;

    @Autowired
    public KafkaWriters(SartKafkaConfiguration kafkaStreamsConfiguration) {
        super();
        this.sartKafkaConfiguration = kafkaStreamsConfiguration;
    }

    @Bean
    public NewTopic domainCommandTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainCommandTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic domainEventTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainEventTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic transactionCommandTopic() {
        return new NewTopic(sartKafkaConfiguration.getTransactionCommandTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic transactionEventTopic() {
        return new NewTopic(sartKafkaConfiguration.getTransactionEventTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic conflictQueryTopic() {
        return new NewTopic(sartKafkaConfiguration.getConflictQueryTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic conflictQueryResultTopic() {
        return new NewTopic(sartKafkaConfiguration.getConflictQueryResultTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic conflictQueryEventTopic() {
        return new NewTopic(sartKafkaConfiguration.getConflictQueryEventTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic domainQueryTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainQueryTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic domainQueryResultTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainQueryResultTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic domainQueryEventTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainQueryEventTopic(), 1, (short) 1);
    }

    @Bean
    @DependsOn("transactionCommandTopic")
    public KafkaTemplate<Long, TransactionCommand> transactionCommandWriter() {
        DefaultKafkaProducerFactory<Long, TransactionCommand> pf = new DefaultKafkaProducerFactory<>(
            sartKafkaConfiguration.getKafkaTransactionProducerConfig());
        KafkaTemplate<Long, TransactionCommand> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getTransactionCommandTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("transactionEventTopic")
    public KafkaTemplate<Long, TransactionEvent> transactionEventWriter() {
        DefaultKafkaProducerFactory<Long, TransactionEvent> pf = new DefaultKafkaProducerFactory<>(
            sartKafkaConfiguration.getKafkaTransactionProducerConfig());
        KafkaTemplate<Long, TransactionEvent> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getTransactionEventTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainCommandTopic")
    public KafkaTemplate<String, DomainCommand> domainCommandWriter() {
        DefaultKafkaProducerFactory<String, DomainCommand> pf = new DefaultKafkaProducerFactory<>(
            sartKafkaConfiguration.getKafkaDomainCommandProducerConfig());
        KafkaTemplate<String, DomainCommand> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainCommandTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainEventTopic")
    public KafkaTemplate<String, DomainEvent<?>> domainEventWriter() {
        DefaultKafkaProducerFactory<String, DomainEvent<?>> pf = new DefaultKafkaProducerFactory<>(
            sartKafkaConfiguration.getKafkaDomainEventProducerConfig());
        KafkaTemplate<String, DomainEvent<?>> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainEventTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("conflictQueryTopic")
    public KafkaTemplate<String, DomainQuery> conflictQueryWriter() {
        DefaultKafkaProducerFactory<String, DomainQuery> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryProducerConfig());
        KafkaTemplate<String, DomainQuery> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getConflictQueryTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("conflictQueryResultTopic")
    public <R extends QueryResult> KafkaTemplate<String, R> conflictQueryResultWriter() {
        DefaultKafkaProducerFactory<String, R> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryResultProducerConfig());
        KafkaTemplate<String, R> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getConflictQueryResultTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("conflictQueryEventTopic")
    public <R extends QueryEvent> KafkaTemplate<String, R> conflictQueryEventWriter() {
        DefaultKafkaProducerFactory<String, R> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryEventProducerConfig());
        KafkaTemplate<String, R> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getConflictQueryEventTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainQueryTopic")
    public KafkaTemplate<String, DomainQuery> domainQueryWriter() {
        DefaultKafkaProducerFactory<String, DomainQuery> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryProducerConfig());
        KafkaTemplate<String, DomainQuery> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainQueryTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainQueryResultTopic")
    public <R extends QueryResult> KafkaTemplate<String, R> domainQueryResultWriter() {
        DefaultKafkaProducerFactory<String, R> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryResultProducerConfig());
        KafkaTemplate<String, R> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainQueryResultTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainQueryEventTopic")
    public <R extends QueryEvent> KafkaTemplate<String, R> domainQueryEventWriter() {
        DefaultKafkaProducerFactory<String, R> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryEventProducerConfig());
        KafkaTemplate<String, R> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainQueryEventTopic());
        return kafkaTemplate;
    }

    public SartKafkaConfiguration getSartKafkaConfiguration() {
        return sartKafkaConfiguration;
    }

}
