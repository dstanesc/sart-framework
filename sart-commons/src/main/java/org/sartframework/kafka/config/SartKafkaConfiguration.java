package org.sartframework.kafka.config;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.apache.kafka.streams.StreamsConfig;
import org.sartframework.kafka.serializers.ProtoSerializer;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "sart.kafka")
public class SartKafkaConfiguration {

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
            return Arrays.asList(domainCommand, domainEvent, transactionCommand, transactionEvent, conflictQuery, conflictQueryResult, conflictQueryEvent, domainQuery, domainQueryResult, domainQueryEvent);
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
 
    public Map<String, Object> getKafkaCommonConfig() {

        Map<String, Object> kafkaConfig = new HashMap<>();
        // kafkaConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, application.id
        // );
        kafkaConfig.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrap.servers);
        
        // Enable record cache of size 10 MB.
        kafkaConfig.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 10 * 1024 * 1024L);
        // Set commit interval to 1 second.
        kafkaConfig.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 500);
        
        //exactly once delivering guarantees, default is StreamsConfig.AT_LEAST_ONCE
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
        kafkaConfig.put(StreamsConfig.APPLICATION_ID_CONFIG, application + "-" + processorIdentifier);
        return kafkaConfig;
    }
    
    public Map<String, Object> getKafkaConsumerProcessorConfig(String groupIdentifier) {
        Map<String, Object> kafkaConfig = getKafkaCommonConfig();
        kafkaConfig.put(ConsumerConfig.GROUP_ID_CONFIG, application + "-" + groupIdentifier);
        kafkaConfig.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ProtoSerializer.class);
        return kafkaConfig;
    }

    public Map<String, Object> getKafkaGenericProducerConfig() {
        Map<String, Object> kafkaConfig = getKafkaCommonConfig();
        kafkaConfig.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ProtoSerializer.class);
        return kafkaConfig;
    }

    public Map<String, Object> getKafkaStreamsTransactionProducerConfig() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.putAll(getKafkaCommonConfig());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);

        return props;
    }

    public Map<String, Object> getKafkaStreamsDomainCommandProducerConfig() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.putAll(getKafkaCommonConfig());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }

    public Map<String, Object> getKafkaStreamsDomainEventProducerConfig() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.putAll(getKafkaCommonConfig());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }
    
    public Map<String, Object> getKafkaStreamsDomainQueryProducerConfig() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.putAll(getKafkaCommonConfig());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }
    
    public Map<String, Object> getKafkaStreamsDomainQueryResultProducerConfig() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.putAll(getKafkaCommonConfig());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }
    
    public Map<String, Object> getKafkaStreamsDomainQueryEventProducerConfig() {
        Map<String, Object> props = new HashMap<String, Object>();
        props.putAll(getKafkaCommonConfig());
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);

        return props;
    }

    public Map<String, Object> getKafkaTransactionProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        kafkaConfig.putAll(getKafkaGenericProducerConfig());
        kafkaConfig.putAll(getKafkaStreamsTransactionProducerConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaDomainCommandProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.putAll(getKafkaGenericProducerConfig());
        kafkaConfig.putAll(getKafkaStreamsDomainCommandProducerConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaDomainEventProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.putAll(getKafkaGenericProducerConfig());
        kafkaConfig.putAll(getKafkaStreamsDomainEventProducerConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public Map<String, Object> getKafkaQueryProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.putAll(getKafkaGenericProducerConfig());
        kafkaConfig.putAll(getKafkaStreamsDomainQueryProducerConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }
    
    public Map<String, Object> getKafkaQueryResultProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.putAll(getKafkaGenericProducerConfig());
        kafkaConfig.putAll(getKafkaStreamsDomainQueryResultProducerConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }
    
    public Map<String, Object> getKafkaQueryEventProducerConfig() {
        final Map<String, Object> kafkaConfig = new HashMap<String, Object>();
        kafkaConfig.putAll(getKafkaGenericProducerConfig());
        kafkaConfig.putAll(getKafkaStreamsDomainQueryEventProducerConfig());

        return Collections.unmodifiableMap(kafkaConfig);
    }

    public String getDomainCommandTopic() {
        return topics.domainCommand;
    }

    public String getDomainEventTopic() {
        return topics.domainEvent;
    }

    public String getTransactionCommandTopic() {
        return topics.transactionCommand;
    }

    public String getTransactionEventTopic() {
        return topics.transactionEvent;
    }
    
    public String getConflictQueryTopic() {
        return topics.conflictQuery;
    }
    
    public String getConflictQueryEventTopic() {
        return topics.conflictQueryEvent;
    }
    
    public String getConflictQueryResultTopic() {
        return topics.conflictQueryResult;
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
}
