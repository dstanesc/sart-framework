package org.sartframework.kafka.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.sartframework.kafka.serializers.SartGenericSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sart.kafka")
public class SartKafkaConfiguration {

    private static final String DASH = "-";

    public static class Topics {

        @NotEmpty
        String domainCommand;

        @NotEmpty
        String domainEvent;

        @NotEmpty
        String transactionCommand;

        @NotEmpty
        String transactionEvent;

        @NotEmpty
        String conflictQuery;

        @NotEmpty
        String conflictQueryResult;

        @NotEmpty
        String conflictQueryEvent;

        @NotEmpty
        String domainQuery;

        @NotEmpty
        String domainQueryResult;

        @NotEmpty
        String domainQueryEvent;

        public String getDomainCommand() {
            return domainCommand;
        }

        public void setDomainCommand(String domainCommand) {
            this.domainCommand = domainCommand;
        }

        public String getDomainEvent() {
            return domainEvent;
        }

        public void setDomainEvent(String domainEvent) {
            this.domainEvent = domainEvent;
        }

        public String getTransactionCommand() {
            return transactionCommand;
        }

        public void setTransactionCommand(String transactionCommand) {
            this.transactionCommand = transactionCommand;
        }

        public String getTransactionEvent() {
            return transactionEvent;
        }

        public void setTransactionEvent(String transactionEvent) {
            this.transactionEvent = transactionEvent;
        }

        public String getConflictQuery() {
            return conflictQuery;
        }

        public void setConflictQuery(String conflictQuery) {
            this.conflictQuery = conflictQuery;
        }

        public String getConflictQueryResult() {
            return conflictQueryResult;
        }

        public void setConflictQueryResult(String conflictQueryResult) {
            this.conflictQueryResult = conflictQueryResult;
        }

        public String getConflictQueryEvent() {
            return conflictQueryEvent;
        }

        public void setConflictQueryEvent(String conflictQueryEvent) {
            this.conflictQueryEvent = conflictQueryEvent;
        }

        public String getDomainQuery() {
            return domainQuery;
        }

        public void setDomainQuery(String domainQuery) {
            this.domainQuery = domainQuery;
        }

        public String getDomainQueryResult() {
            return domainQueryResult;
        }

        public void setDomainQueryResult(String domainQueryResult) {
            this.domainQueryResult = domainQueryResult;
        }

        public String getDomainQueryEvent() {
            return domainQueryEvent;
        }

        public void setDomainQueryEvent(String domainQueryEvent) {
            this.domainQueryEvent = domainQueryEvent;
        }

        public List<String> getAll() {
            return Arrays.asList(domainCommand, domainEvent, transactionCommand, transactionEvent, conflictQuery, conflictQueryResult,
                conflictQueryEvent, domainQuery, domainQueryResult, domainQueryEvent);
        }
    }

    public static class Bootstrap {

        @NotEmpty
        String servers;

        public String getServers() {
            return servers;
        }

        public void setServers(String servers) {
            this.servers = servers;
        }
    }

    public static class Transaction {

        @NotNull
        Store store;

        public Store getStore() {
            return store;
        }

        public void setStore(Store store) {
            this.store = store;
        }
    }

    public static class Aggregate {

        @NotNull
        Store store;

        public Store getStore() {
            return store;
        }

        public void setStore(Store store) {
            this.store = store;
        }
    }

    public static class Store {

        @NotEmpty
        String name;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }
    }

    public static class Server {

        @NotEmpty
        Integer port;

        public Integer getPort() {
            return port;
        }

        public void setPort(Integer port) {
            this.port = port;
        }
    }

    public static class System {

        @NotEmpty
        String id;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }
    }

    @NotNull
    Topics topics;

    @NotEmpty
    String application;

    @NotNull
    Bootstrap bootstrap;

    @NotNull
    Transaction transaction;

    @NotNull
    Aggregate aggregate;

    @NotNull
    System system;

    public Map<String, Object> getKafkaCommonConfig() {

        Map<String, Object> kafkaConfig = new HashMap<>();
        // kafkaConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, application.id
        // );
        kafkaConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap.servers);

        // Enable record cache of size 10 MB.
        kafkaConfig.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
        // Set commit interval to 1 second.
        kafkaConfig.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10);

        // exactly once delivering guarantees, default is
        // StreamsConfig.AT_LEAST_ONCE
        kafkaConfig.put(StreamsConfig.PROCESSING_GUARANTEE_CONFIG, StreamsConfig.EXACTLY_ONCE);

        kafkaConfig.put(ProducerConfig.ACKS_CONFIG, "all");
        kafkaConfig.put(ProducerConfig.RETRIES_CONFIG, 3);
        kafkaConfig.put(ProducerConfig.BATCH_SIZE_CONFIG, 16384);
        kafkaConfig.put(ProducerConfig.LINGER_MS_CONFIG, 1);
        kafkaConfig.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 33554432);

        return kafkaConfig;
    }

    public Map<String, Object> getKafkaStreamsProcessorConfig(String processorIdentifier) {
        Map<String, Object> kafkaConfig = getKafkaCommonConfig();
        kafkaConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, system.id + DASH + application + DASH + processorIdentifier);
        return kafkaConfig;
    }

    public Map<String, Object> getKafkaTransactionCommandProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SartGenericSerializer.class);
        kafkaConfig.putAll(getKafkaCommonConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaTransactionEventProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SartGenericSerializer.class);
        kafkaConfig.putAll(getKafkaCommonConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaDomainCommandProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SartGenericSerializer.class);
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaConfig.putAll(getKafkaCommonConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaDomainEventProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SartGenericSerializer.class);
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaConfig.putAll(getKafkaCommonConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaDomainQueryProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SartGenericSerializer.class);
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaConfig.putAll(getKafkaCommonConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaQueryResultProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SartGenericSerializer.class);
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaConfig.putAll(getKafkaCommonConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaQueryEventProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, SartGenericSerializer.class);
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        kafkaConfig.putAll(getKafkaCommonConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public String getAggregateStoreName() {

        return getSystemPrefixedName(getAggregate().getStore().getName());
    }

    public String getTransactionStoreName() {

        return getSystemPrefixedName(getTransaction().getStore().getName());
    }

    public String getSystemPrefixedName(String name) {

        return system.id + DASH + name;
    }

    public String getDomainCommandTopic() {
        return system.id + DASH + topics.domainCommand;
    }

    public String getDomainEventTopic() {
        return system.id + DASH + topics.domainEvent;
    }

    public String getTransactionCommandTopic() {
        return system.id + DASH + topics.transactionCommand;
    }

    public String getTransactionEventTopic() {
        return system.id + DASH + topics.transactionEvent;
    }

    public String getConflictQueryTopic() {
        return system.id + DASH + topics.conflictQuery;
    }

    public String getConflictQueryEventTopic() {
        return system.id + DASH + topics.conflictQueryEvent;
    }

    public String getConflictQueryResultTopic() {
        return system.id + DASH + topics.conflictQueryResult;
    }

    public String getDomainQueryTopic() {
        return topics.domainQuery;
    }

    public String getDomainQueryResultTopic() {
        return topics.domainQueryResult;
    }

    public String getDomainQueryEventTopic() {
        return topics.domainQueryEvent;
    }

    public Topics getTopics() {
        return topics;
    }

    public void setTopics(Topics topics) {
        this.topics = topics;
    }

    public Bootstrap getBootstrap() {
        return bootstrap;
    }

    public void setBootstrap(Bootstrap bootstrap) {
        this.bootstrap = bootstrap;
    }

    public String getApplication() {
        return application;
    }

    public void setApplication(String application) {
        this.application = application;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public Aggregate getAggregate() {
        return aggregate;
    }

    public void setAggregate(Aggregate aggregate) {
        this.aggregate = aggregate;
    }

    public System getSystem() {
        return system;
    }

    public void setSystem(System cluster) {
        this.system = cluster;
    }

    public String getSid() {
        return system.id;
    }
}
