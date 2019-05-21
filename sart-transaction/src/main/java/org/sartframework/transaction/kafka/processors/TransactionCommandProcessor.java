package org.sartframework.transaction.kafka.processors;

import org.apache.kafka.streams.processor.Processor;
import org.apache.kafka.streams.processor.ProcessorContext;
import org.apache.kafka.streams.state.KeyValueStore;
import org.sartframework.command.transaction.CreateTransactionCommand;
import org.sartframework.command.transaction.TransactionCommand;
import org.sartframework.error.transaction.SystemFault;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.transaction.BusinessTransactionManager;
import org.sartframework.transaction.kafka.KafkaTransactionAggregate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TransactionCommandProcessor implements Processor<Long, TransactionCommand> {

    final static Logger LOGGER = LoggerFactory.getLogger(TransactionCommandProcessor.class);

    private ProcessorContext context;

    private KeyValueStore<Long, KafkaTransactionAggregate> aggregateStore;

    final SartKafkaConfiguration kafkaStreamsConfiguration;
    
    final BusinessTransactionManager businessTransactionManager;
    
    final KafkaStreamsContext streamsContext;

    public TransactionCommandProcessor(SartKafkaConfiguration kafkaStreamsConfiguration, BusinessTransactionManager businessTransactionManager, KafkaStreamsContext streamsContext) {
        super();
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.businessTransactionManager = businessTransactionManager;
        this.streamsContext = streamsContext;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void init(ProcessorContext context) {

        this.context = context;

        this.aggregateStore = (KeyValueStore<Long, KafkaTransactionAggregate>) context
            .getStateStore(kafkaStreamsConfiguration.getTransactionStoreName());
        
        this.streamsContext.initContext(context);
    }

    @Override
    public void process(Long xid, TransactionCommand transactionCommand) {

        LOGGER.info("Process transaction command for xid={}, offset={}, partition={}, {}", xid,  context.offset(), context.partition(), transactionCommand);

        KafkaTransactionAggregate txnAggregate = aggregateStore.get(xid);

        if (txnAggregate == null) {

            if (transactionCommand instanceof CreateTransactionCommand) {

                txnAggregate = new KafkaTransactionAggregate();
                txnAggregate.setPartition(context.partition());
                txnAggregate.setOffset(context.offset());

            } else {
                
                streamsContext.publish( new SystemFault(xid, new RuntimeException("invalid transaction command " + transactionCommand)));
            }
        }

        txnAggregate.setPublisher(streamsContext);
        
        txnAggregate.handle(transactionCommand);

        aggregateStore.put(xid, txnAggregate);

        context.commit();
    }

    @Override
    public void punctuate(long timestamp) {

    }

    @Override
    public void close() {

    }

}
