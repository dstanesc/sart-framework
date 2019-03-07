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
    private NewTopic domainCommandTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainCommandTopic(), 1, (short) 1);
    }

    @Bean
    private NewTopic domainEventTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainEventTopic(), 1, (short) 1);
    }

    @Bean
    public NewTopic transactionCommandTopic() {
        return new NewTopic(sartKafkaConfiguration.getTransactionCommandTopic(), 1, (short) 1);
    }

    @Bean
    private NewTopic transactionEventTopic() {
        return new NewTopic(sartKafkaConfiguration.getTransactionEventTopic(), 1, (short) 1);
    }

    @Bean
    private NewTopic conflictQueryTopic() {
        return new NewTopic(sartKafkaConfiguration.getConflictQueryTopic(), 1, (short) 1);
    }

    @Bean
    private NewTopic conflictQueryResultTopic() {
        return new NewTopic(sartKafkaConfiguration.getConflictQueryResultTopic(), 1, (short) 1);
    }

    @Bean
    private NewTopic conflictQueryEventTopic() {
        return new NewTopic(sartKafkaConfiguration.getConflictQueryEventTopic(), 1, (short) 1);
    }

    @Bean
    private NewTopic domainQueryTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainQueryTopic(), 1, (short) 1);
    }

    @Bean
    private NewTopic domainQueryResultTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainQueryResultTopic(), 1, (short) 1);
    }

    @Bean
    private NewTopic domainQueryEventTopic() {
        return new NewTopic(sartKafkaConfiguration.getDomainQueryEventTopic(), 1, (short) 1);
    }

    @Bean
    @DependsOn("transactionCommandTopic")
    private KafkaTemplate<Long, TransactionCommand> transactionCommandWriter() {
        DefaultKafkaProducerFactory<Long, TransactionCommand> pf = new DefaultKafkaProducerFactory<>(
            sartKafkaConfiguration.getKafkaTransactionProducerConfig());
        KafkaTemplate<Long, TransactionCommand> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getTransactionCommandTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("transactionEventTopic")
    private KafkaTemplate<Long, TransactionEvent> transactionEventWriter() {
        DefaultKafkaProducerFactory<Long, TransactionEvent> pf = new DefaultKafkaProducerFactory<>(
            sartKafkaConfiguration.getKafkaTransactionProducerConfig());
        KafkaTemplate<Long, TransactionEvent> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getTransactionEventTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainCommandTopic")
    private KafkaTemplate<String, DomainCommand> domainCommandWriter() {
        DefaultKafkaProducerFactory<String, DomainCommand> pf = new DefaultKafkaProducerFactory<>(
            sartKafkaConfiguration.getKafkaDomainCommandProducerConfig());
        KafkaTemplate<String, DomainCommand> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainCommandTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainEventTopic")
    private KafkaTemplate<String, DomainEvent<?>> domainEventWriter() {
        DefaultKafkaProducerFactory<String, DomainEvent<?>> pf = new DefaultKafkaProducerFactory<>(
            sartKafkaConfiguration.getKafkaDomainEventProducerConfig());
        KafkaTemplate<String, DomainEvent<?>> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainEventTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("conflictQueryTopic")
    private KafkaTemplate<String, DomainQuery> conflictQueryWriter() {
        DefaultKafkaProducerFactory<String, DomainQuery> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryProducerConfig());
        KafkaTemplate<String, DomainQuery> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getConflictQueryTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("conflictQueryResultTopic")
    private <R extends QueryResult> KafkaTemplate<String, R> conflictQueryResultWriter() {
        DefaultKafkaProducerFactory<String, R> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryResultProducerConfig());
        KafkaTemplate<String, R> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getConflictQueryResultTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("conflictQueryEventTopic")
    private <R extends QueryEvent> KafkaTemplate<String, R> conflictQueryEventWriter() {
        DefaultKafkaProducerFactory<String, R> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryEventProducerConfig());
        KafkaTemplate<String, R> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getConflictQueryEventTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainQueryTopic")
    private KafkaTemplate<String, DomainQuery> domainQueryWriter() {
        DefaultKafkaProducerFactory<String, DomainQuery> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryProducerConfig());
        KafkaTemplate<String, DomainQuery> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainQueryTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainQueryResultTopic")
    private <R extends QueryResult> KafkaTemplate<String, R> domainQueryResultWriter() {
        DefaultKafkaProducerFactory<String, R> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryResultProducerConfig());
        KafkaTemplate<String, R> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainQueryResultTopic());
        return kafkaTemplate;
    }

    @Bean
    @DependsOn("domainQueryEventTopic")
    private <R extends QueryEvent> KafkaTemplate<String, R> domainQueryEventWriter() {
        DefaultKafkaProducerFactory<String, R> pf = new DefaultKafkaProducerFactory<>(sartKafkaConfiguration.getKafkaQueryEventProducerConfig());
        KafkaTemplate<String, R> kafkaTemplate = new KafkaTemplate<>(pf, true);
        kafkaTemplate.setDefaultTopic(sartKafkaConfiguration.getDomainQueryEventTopic());
        return kafkaTemplate;
    }

  
    
    @Autowired
    KafkaTemplate<Long, TransactionCommand> transactionCommandWriter;
    
    @Autowired
    KafkaTemplate<Long, TransactionEvent> transactionEventWriter;

    @Autowired
    KafkaTemplate<String, DomainCommand> domainCommandWriter;
    
    @Autowired
    KafkaTemplate<String, DomainEvent<?>> domainEventWriter;
    
    @Autowired
    KafkaTemplate<String, DomainQuery> conflictQueryWriter;
    
    @Autowired
    KafkaTemplate<String, QueryResult> conflictQueryResultWriter;
    
    @Autowired
    KafkaTemplate<String, QueryEvent> conflictQueryEventWriter;
    
    @Autowired
    KafkaTemplate<String, DomainQuery> domainQueryWriter;
    
    @Autowired
    KafkaTemplate<String, QueryResult> domainQueryResultWriter;
    
    @Autowired
    KafkaTemplate<String, QueryEvent> domainQueryEventWriter;

    
    public KafkaTemplate<Long, TransactionCommand> getTransactionCommandWriter() {
        return transactionCommandWriter;
    }

    public KafkaTemplate<Long, TransactionEvent> getTransactionEventWriter() {
        return transactionEventWriter;
    }

    public KafkaTemplate<String, DomainCommand> getDomainCommandWriter() {
        return domainCommandWriter;
    }

    public KafkaTemplate<String, DomainEvent<?>> getDomainEventWriter() {
        return domainEventWriter;
    }

    public KafkaTemplate<String, DomainQuery> getConflictQueryWriter() {
        return conflictQueryWriter;
    }

    @SuppressWarnings("unchecked")
    public <R extends QueryResult>  KafkaTemplate<String, R> getConflictQueryResultWriter() {
        return (KafkaTemplate<String, R>) conflictQueryResultWriter;
    }

    @SuppressWarnings("unchecked")
    public <E extends QueryEvent> KafkaTemplate<String, E> getConflictQueryEventWriter() {
        return (KafkaTemplate<String, E>) conflictQueryEventWriter;
    }

    public KafkaTemplate<String, DomainQuery> getDomainQueryWriter() {
        return domainQueryWriter;
    }

    @SuppressWarnings("unchecked")
    public <R extends QueryResult> KafkaTemplate<String, R> getDomainQueryResultWriter() {
        return (KafkaTemplate<String, R>) domainQueryResultWriter;
    }

    @SuppressWarnings("unchecked")
    public <E extends QueryEvent> KafkaTemplate<String, E> getDomainQueryEventWriter() {
        return (KafkaTemplate<String, E>) domainQueryEventWriter;
    }

    public SartKafkaConfiguration getSartKafkaConfiguration() {
        return sartKafkaConfiguration;
    }

}
