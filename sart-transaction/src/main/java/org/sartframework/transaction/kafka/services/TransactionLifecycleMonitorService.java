package org.sartframework.transaction.kafka.services;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.Consumed;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.Topology;
import org.apache.kafka.streams.kstream.KStream;
import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.EventDescriptor;
import org.sartframework.event.TransactionEvent;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.event.transaction.ProgressLoggedEvent;
import org.sartframework.event.transaction.TransactionAbortedEvent;
import org.sartframework.event.transaction.TransactionCommittedEvent;
import org.sartframework.event.transaction.TransactionCompletedEvent;
import org.sartframework.event.transaction.TransactionDetailsAttachedEvent;
import org.sartframework.event.transaction.TransactionStartedEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.TransactionDetails;
import org.sartframework.transaction.TransactionMonitors;
import org.sartframework.transaction.kafka.KafkaBusinessTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.ReplayProcessor;

@Component
public class TransactionLifecycleMonitorService implements ManagedService<TransactionLifecycleMonitorService> {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionLifecycleMonitorService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final KafkaBusinessTransactionManager businessTransactionManager;

    KafkaStreams kafkaStreams;

    Map<Long, TransactionMonitors> subscribedMonitors = new HashMap<>();
    
    ExecutorService executor = Executors.newFixedThreadPool(5);
    
    ReplayProcessor<EventDescriptor> eventDescriptorFlux = ReplayProcessor.<EventDescriptor> create(1000);
    
    @Autowired
    public TransactionLifecycleMonitorService(SartKafkaConfiguration kafkaStreamsConfiguration,
                                              KafkaBusinessTransactionManager businessTransactionManager) {
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.businessTransactionManager = businessTransactionManager;
    }

    @Override
    public TransactionLifecycleMonitorService start() {

        LOGGER.info("Starting transaction lifecycle monitor service");

        StreamsBuilder builder = new StreamsBuilder();

        KStream<Long, TransactionEvent> transactionEventStream = builder.stream(kafkaStreamsConfiguration.getTransactionEventTopic(),
            Consumed.<Long, TransactionEvent> with(Serdes.Long(), SartSerdes.TransactionEventSerde()));

        transactionEventStream.foreach((xid, event) -> {

            TransactionMonitors transactionMonitors = getSubscribedMonitors(xid);

            if (transactionMonitors != null) {
                executor.submit(() -> dispatchEvent(transactionMonitors, event));
            }
        });

        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("transaction-lifecycle-monitor")));

        kafkaStreams.start();

        businessTransactionManager.registerTransactionLifecycleMonitorService(this);

        return this;
    }

    protected void dispatchEvent(TransactionMonitors transactionMonitors, TransactionEvent event) {

        if (event instanceof TransactionStartedEvent) {
            transactionMonitors.onNextStart((TransactionStartedEvent) event);
            EventDescriptor eventDescriptor = describe(transactionMonitors.getXid(), -1L, event);
            eventDescriptorFlux.onNext(eventDescriptor);
        } else if (event instanceof TransactionCommittedEvent) {
            transactionMonitors.onNextCommit((TransactionCommittedEvent) event);
            EventDescriptor eventDescriptor = describe(transactionMonitors.getXid(), -1L, event);
            eventDescriptorFlux.onNext(eventDescriptor);
        }else if (event instanceof TransactionCompletedEvent) {
            transactionMonitors.onNextComplete((TransactionCompletedEvent) event);
        } else if (event instanceof TransactionAbortedEvent) {
            transactionMonitors.onNextAbort((TransactionAbortedEvent) event);
            EventDescriptor eventDescriptor = describe(transactionMonitors.getXid(), -1L, event);
            eventDescriptorFlux.onNext(eventDescriptor);
        } else if (event instanceof TransactionDetailsAttachedEvent) {
            transactionMonitors.onNextDetailsAttached((TransactionDetailsAttachedEvent) event);
            EventDescriptor eventDescriptor = describe(transactionMonitors.getXid(), -1L, event);
            eventDescriptorFlux.onNext(eventDescriptor);
        } else if (event instanceof ConflictResolvedEvent) {
            transactionMonitors.onNextConflict((ConflictResolvedEvent) event);
        } else if (event instanceof ProgressLoggedEvent) {
            ProgressLoggedEvent progressEvent = (ProgressLoggedEvent) event;
            transactionMonitors.onNextLogged(progressEvent);
            EventDescriptor eventDescriptor = describe(transactionMonitors.getXid(), progressEvent.getXcs(), event);
            eventDescriptorFlux.onNext(eventDescriptor);
        }
    }

    private EventDescriptor describe(Long xid, Long xcs, TransactionEvent event) {
        EventDescriptor eventDescriptor = new EventDescriptor();
        eventDescriptor.setEventName(event.getClass().getSimpleName());
        if (event instanceof ProgressLoggedEvent) {
            ProgressLoggedEvent progressEvent = (ProgressLoggedEvent) event;
            DomainEvent<? extends DomainCommand> domainEvent = progressEvent.getDomainEvent();
            eventDescriptor.setEventDetail(domainEvent.getClass().getSimpleName());
        } else if (event instanceof TransactionDetailsAttachedEvent) {
            TransactionDetailsAttachedEvent detailsAttachedEvent = (TransactionDetailsAttachedEvent) event;
            TransactionDetails transactionDetails = detailsAttachedEvent.getDetails();
            ObjectMapper objectMapper = new ObjectMapper();
            try {
                String payload = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(transactionDetails);
                eventDescriptor.setEventDetail(payload);
            } catch (JsonProcessingException e) {
                LOGGER.error("Cannot serialize to json", e);
            }
        }
        eventDescriptor.setCreationTime(event.getCreationTime());
        eventDescriptor.setXid(xid);
        eventDescriptor.setXcs(xcs);
        return eventDescriptor;
    }

    public synchronized TransactionMonitors getSubscribedMonitors(long xid) {
        
        TransactionMonitors transactionMonitors = subscribedMonitors.get(xid);
        
        return transactionMonitors;
    }
    
    public void registerMonitors(long xid) {
        
        TransactionMonitors transactionMonitors = new TransactionMonitors(xid);
        
        subscribedMonitors.put(xid, transactionMonitors);
    }
    
    public synchronized TransactionMonitors unregisterMonitors(long xid) {
        
        LOGGER.info("Unregister subscribed monitors for xid={}", xid);
        
        TransactionMonitors  transactionMonitors = subscribedMonitors.remove(xid);
        
        //transactionMonitors.close();
        
        return transactionMonitors;
    }

    @Override
    public TransactionLifecycleMonitorService stop() {

        LOGGER.info("Stopping transaction lifecycle monitor service");

        kafkaStreams.close();

        return this;
    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }

    public ReplayProcessor<EventDescriptor> getEventDescriptorFlux() {
        return eventDescriptorFlux;
    }
}
