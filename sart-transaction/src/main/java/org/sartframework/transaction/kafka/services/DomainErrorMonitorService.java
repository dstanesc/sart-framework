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
import org.sartframework.error.DomainError;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.kafka.serializers.serde.SartSerdes;
import org.sartframework.service.ManagedService;
import org.sartframework.transaction.DomainErrorMonitors;
import org.sartframework.transaction.kafka.KafkaBusinessTransactionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DomainErrorMonitorService implements ManagedService<DomainErrorMonitorService> {
    
    final static Logger LOGGER = LoggerFactory.getLogger(DomainErrorMonitorService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;
    
    final KafkaBusinessTransactionManager businessTransactionManager;
    
    KafkaStreams kafkaStreams;
    
    Map<Long, DomainErrorMonitors> subscribedMonitors = new HashMap<>();
    
    ExecutorService executor = Executors.newFixedThreadPool(1);
    
    @Autowired
    public DomainErrorMonitorService(SartKafkaConfiguration kafkaStreamsConfiguration, KafkaBusinessTransactionManager businessTransactionManager) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.businessTransactionManager = businessTransactionManager;
    }

    @Override
    public DomainErrorMonitorService start() {
        
        LOGGER.info("Starting domain error monitor service");
        
        StreamsBuilder builder = new StreamsBuilder();
        
        KStream<Long, DomainError> domainErrorStream = builder.stream(kafkaStreamsConfiguration.getDomainErrorTopic(),
            Consumed.<Long, DomainError> with(Serdes.Long(), SartSerdes.DomainErrorSerde()));
        
        domainErrorStream.foreach((xid, domainError) -> {

            DomainErrorMonitors domainErrorMonitors = getSubscribedMonitors(xid);

            if (domainErrorMonitors != null) {
                
                executor.submit(() -> dispatchError(domainErrorMonitors, domainError));
            }
        });
        
        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("domain-error-monitor")));

        kafkaStreams.start();

        businessTransactionManager.registerDomainErrorMonitorService(this);
        
        return this;
    }


    private void dispatchError(DomainErrorMonitors domainErrorMonitors, DomainError domainError) {
       
        LOGGER.info("Dispatch domain error{}", domainError);
        
        domainErrorMonitors.onNextDomainError(domainError);
    }


    public synchronized DomainErrorMonitors getSubscribedMonitors(long xid) {
        
        DomainErrorMonitors domainErrorMonitors = subscribedMonitors.get(xid);
        
        return domainErrorMonitors;
    }
    
    public void registerMonitors(long xid) {

        DomainErrorMonitors domainErrorMonitors = new DomainErrorMonitors(xid);

        subscribedMonitors.put(xid, domainErrorMonitors);
    }
    
    public synchronized DomainErrorMonitors unregisterMonitors(long xid) {
        
        LOGGER.info("Unregister subscribed domain error monitors for xid={}", xid);
        
        DomainErrorMonitors  domainErrorMonitors = subscribedMonitors.remove(xid);
        
        //domainErrorMonitors.close();
        
        return domainErrorMonitors;
    }
    
    @Override
    public DomainErrorMonitorService stop() {
        
        LOGGER.info("Stopping domain error monitor service");

        kafkaStreams.close();

        return this;
    }
    
    
}
