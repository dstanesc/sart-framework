package org.sartframework.driver;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientDomainErrorMonitorService implements ManagedService<ClientDomainErrorMonitorService> {

    final static Logger LOGGER = LoggerFactory.getLogger(ClientDomainErrorMonitorService.class);

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final DomainErrorMonitors domainErrorMonitors;
    
    KafkaStreams kafkaStreams;
    
    ExecutorService executor = Executors.newFixedThreadPool(1);

    public ClientDomainErrorMonitorService(SartKafkaConfiguration kafkaStreamsConfiguration, Long xid) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.domainErrorMonitors = new DomainErrorMonitors(xid);
    }

    @Override
    public ClientDomainErrorMonitorService start() {
       
        LOGGER.info("Starting domain error monitor");
        
        StreamsBuilder builder = new StreamsBuilder();
        
        KStream<Long, DomainError> domainErrorStream = builder.stream(kafkaStreamsConfiguration.getDomainErrorTopic(),
            Consumed.<Long, DomainError> with(Serdes.Long(), SartSerdes.DomainErrorSerde()));
        
        domainErrorStream
        
        .filter((xid, error)-> domainErrorMonitors.getXid() == xid)
        
        .foreach((xid, error)-> {
            
                executor.submit(() -> dispatchError(error));
        });
        
        Topology monitorTopology = builder.build();

        kafkaStreams = new KafkaStreams(monitorTopology,
            new StreamsConfig(kafkaStreamsConfiguration.getKafkaStreamsProcessorConfig("domain-error-monitor")));

        kafkaStreams.start();

        return this;
        
    }

    private void dispatchError(DomainError domainError) {

        domainErrorMonitors.onNextDomainError(domainError);
    }

    @Override
    public ClientDomainErrorMonitorService stop() {
        
        LOGGER.info("Stopping domain error monitor");

        kafkaStreams.close();
        
        return this;
    }

    public DomainErrorMonitors getDomainErrorMonitors() {
        return domainErrorMonitors;
    }

    public KafkaStreams getKafkaStreams() {
        return kafkaStreams;
    }
}
