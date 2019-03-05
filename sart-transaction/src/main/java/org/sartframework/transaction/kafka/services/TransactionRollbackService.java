package org.sartframework.transaction.kafka.services;

import java.util.ArrayList;
import java.util.List;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.Produced;
import org.sartframework.command.DomainCommand;
import org.sartframework.command.transaction.CompensateDomainEventCommand;
import org.sartframework.event.AggregateCreatedEvent;
import org.sartframework.event.AggregateFieldUpdatedEvent;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.transaction.ProgressLoggedEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.kafka.KafkaBusinessTransactionManager;
import org.sartframework.transaction.kafka.PartitionOffset;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionRollbackService implements ManagedService<TransactionRollbackService> {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionRollbackService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final long xid;

    final PartitionOffset startOffset;
    
    KafkaStreams kafkaStreams;

    public TransactionRollbackService(long xid, PartitionOffset startOffset, SartKafkaConfiguration kafkaStreamsConfiguration) {
        super();
        this.xid = xid;
        this.startOffset = startOffset;
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
    }

    @Override
    public TransactionRollbackService start() {

        LOGGER.info("Starting transaction rollback service for xid={}, offset={}", xid, startOffset);

//        Map<String, Object> kafkaConsumerConfig = kafkaStreamsConfiguration
//            .getKafkaConsumerProcessorConfig("transaction-rollback-consumer-group-" + xid);
//
//        TopicPartition partition = new TopicPartition(kafkaStreamsConfiguration.getTransactionEventTopic(), startOffset.getPartition());
//
//        KafkaConsumer<Long, TransactionEvent> rollbackConsumer = new KafkaConsumer<Long, TransactionEvent>(kafkaConsumerConfig,
//            Serdes.Long().deserializer(), SartSerdes.<TransactionEvent> Hello().deserializer());
//
//        rollbackConsumer.assign(Arrays.asList(partition));
//
//        rollbackConsumer.seek(partition, startOffset.getOffset());
//
//        List<String> aggregateCreations = new ArrayList<>();
//
//        List<String> aggregateFieldUpdates = new ArrayList<>();
//
//        exit: while (true) {
//
//            ConsumerRecords<Long, TransactionEvent> consumerRecords = rollbackConsumer.poll(1000);
//
//            for (ConsumerRecord<Long, TransactionEvent> consumerRecord : consumerRecords) {
//
//                TransactionEvent transactionEvent = consumerRecord.value();
//
//                if (xid == transactionEvent.getXid()) {
//
//                    LOGGER.info("Processing transaction rollback exent {} for xid={}", transactionEvent, xid);
//
//                    if (transactionEvent instanceof ProgressLoggedEvent) {
//
//                        ProgressLoggedEvent progressEvent = (ProgressLoggedEvent) transactionEvent;
//
//                        long xcs = progressEvent.getXcs();
//
//                        if (xcs > 0) {
//
//                            DomainEvent<? extends DomainCommand> domainEvent = progressEvent.getDomainEvent();
//                            boolean skip = false;
//                            if (domainEvent instanceof AggregateCreatedEvent) {
//                                aggregateCreations.add(domainEvent.getAggregateKey());
//                            } else if (aggregateCreations.contains(domainEvent.getAggregateKey())) {
//                                skip = true; // will apply a null operation
//                                             // on the domain aggregate
//                            } else if (domainEvent instanceof AggregateFieldUpdatedEvent) {
//                                String updateKey = domainEvent.getAggregateKey() + "_" + domainEvent.getChangeKey();
//                                if (aggregateFieldUpdates.contains(updateKey)) {
//                                    skip = true;
//                                } else {
//                                    aggregateFieldUpdates.add(updateKey);
//                                }
//                            }
//
//                            CompensateDomainEventCommand compensateCommand = new CompensateDomainEventCommand(xid, xcs, domainEvent, skip);
//
//                            LOGGER.info("Processing transaction rollback, dispatching {}", compensateCommand);
//
//                            dispatch(compensateCommand);
//                        }
//
//                    } else if (transactionEvent instanceof TransactionAbortedEvent) {
//
//                        LOGGER.info("Processing transaction rollback finished for xid={}", xid);
//
//                        break exit;
//                    }
//                }
//            }
//
//            rollbackConsumer.commitAsync();
//
//            try {
//
//                Thread.sleep(3000);
//
//            } catch (InterruptedException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        rollbackConsumer.close();
        
        StreamsBuilder builder = new StreamsBuilder();

        List<String> aggregateCreations = new ArrayList<>();

        List<String> aggregateFieldUpdates = new ArrayList<>();

        builder
            .stream(kafkaStreamsConfiguration.getTransactionEventTopic(), Consumed.<Long, TransactionEvent> with(Serdes.Long(), SartSerdes.Proto()))

            .filter((x, e) -> (x == xid)).filter((x, e) -> ProgressLoggedEvent.class.equals(e.getClass()) && ((ProgressLoggedEvent) e).getXcs() > 0)

            .mapValues(e -> {

                ProgressLoggedEvent progressEvent = ((ProgressLoggedEvent) e);

                long xcs = progressEvent.getXcs();

                DomainEvent<? extends DomainCommand> domainEvent = progressEvent.getDomainEvent();
                
              boolean skip = false;
              
              if (domainEvent instanceof AggregateCreatedEvent) {
                  aggregateCreations.add(domainEvent.getAggregateKey());
              } else if (aggregateCreations.contains(domainEvent.getAggregateKey())) {
                  skip = true;
              } else if (domainEvent instanceof AggregateFieldUpdatedEvent) {
                  String updateKey = domainEvent.getAggregateKey() + "_" + domainEvent.getChangeKey();
                  if (aggregateFieldUpdates.contains(updateKey)) {
                      skip = true;
                  } else {
                      aggregateFieldUpdates.add(updateKey);
                  }
              }

                return new CompensateDomainEventCommand(xid, xcs, domainEvent, skip);
            })

            .to(kafkaStreamsConfiguration.getTransactionCommandTopic(),
                Produced.<Long, CompensateDomainEventCommand> with(Serdes.Long(), SartSerdes.Proto()));

        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("transaction-rollback-service-" + xid)));

        kafkaStreams.start();

        return this;
    }

    private void dispatch(CompensateDomainEventCommand compensateCommand) {

        KafkaBusinessTransactionManager.get().publish(compensateCommand);
    }

    @Override
    public TransactionRollbackService stop() {

        LOGGER.info("Stopping transaction rollback service for xid={}", xid);
        
        kafkaStreams.close();

        return this;
    }

    public long getXid() {
        return xid;
    }

}
