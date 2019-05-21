package org.sartframework.transaction.kafka.processors;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.sartframework.aggregate.AnnotatedDomainAggregate;
import org.sartframework.aggregate.HandlerNotFound;
import org.sartframework.command.CreateAggregateCommand;
import org.sartframework.command.DomainCommand;
import org.sartframework.error.transaction.SystemFault;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DomainCommandProcessor implements Processor<String, DomainCommand> {

    final static Logger LOGGER = LoggerFactory.getLogger(DomainCommandProcessor.class);

    private ProcessorContext context;

    private KeyValueStore<String, AnnotatedDomainAggregate> aggregateStore;

    final SartKafkaConfiguration kafkaStreamsConfiguration;

    final KafkaStreamsContext streamsContext;

    public DomainCommandProcessor(SartKafkaConfiguration kafkaStreamsConfiguration, KafkaStreamsContext streamsContext) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.streamsContext = streamsContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(ProcessorContext context) {

        this.context = context;

        this.aggregateStore = (KeyValueStore<String, AnnotatedDomainAggregate>) context
            .getStateStore(kafkaStreamsConfiguration.getAggregateStoreName());

        this.streamsContext.initContext(context);
    }

    @Override
    public void process(String aggregateKey, DomainCommand domainCommand) {

        processCommandInternal(aggregateKey, domainCommand);
    }

    protected void processCommandInternal(String aggregateKey, DomainCommand domainCommand) {
        long xid = domainCommand.getXid();

        LOGGER.info("Process domain command for xid={}, xcs={}, {}", xid, domainCommand.getXcs(), domainCommand);

        AnnotatedDomainAggregate aggregate = aggregateStore.get(aggregateKey);

        if (aggregate == null) {

            if (domainCommand instanceof CreateAggregateCommand) {

                CreateAggregateCommand creationCommand = (CreateAggregateCommand) domainCommand;

                aggregate = (AnnotatedDomainAggregate) creationCommand.newAggregate();
                
            } else {
                
                LOGGER.error("invalid domain command or out of order command sequence " + domainCommand);
                
                streamsContext.publish( new SystemFault(xid, new RuntimeException("invalid domain command or out of order command sequence " + domainCommand)));
            
                context.commit();
            }
        }

        if (aggregate != null) {

            try {

                aggregate.setPublisher(streamsContext);

                aggregate.handle(domainCommand);

                aggregateStore.put(aggregateKey, aggregate);

                context.commit();

            } catch (HandlerNotFound e) {

                LOGGER.error("Handler missing in aggregate. Use @DomainCommandHandler annotation on {}#methodName({} domainCommand)",
                    e.getHandlingClass(), e.getArgumentType());
                LOGGER.error("Handler missing in aggregate", e);
                
                streamsContext.publish( new SystemFault(xid, e));
                
                context.commit();
            }
        }
    }

    @Override
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }

}
