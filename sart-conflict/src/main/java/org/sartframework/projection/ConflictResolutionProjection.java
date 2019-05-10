package org.sartframework.projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.sartframework.annotation.DomainEventHandler;
import org.sartframework.annotation.DomainQueryHandler;
import org.sartframework.event.QueryEvent;
import org.sartframework.event.transaction.ConflictResolvedData;
import org.sartframework.event.transaction.ConflictResolvedEvent;
import org.sartframework.kafka.channels.KafkaWriters;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.projection.kafka.query.KafkaTransactionProjection;
import org.sartframework.projection.kafka.query.KafkaTransactionQueryResultsEmitter;
import org.sartframework.query.AbstractQuery;
import org.sartframework.query.ConflictsByAggregateQuery;
import org.sartframework.query.ConflictsByChangeQuery;
import org.sartframework.query.ConflictsByXidQuery;
import org.sartframework.query.DomainQuery;
import org.sartframework.query.QueryResultsEmitter;
import org.sartframework.result.ConflictResolvedResult;
import org.sartframework.result.EmptyResult;
import org.sartframework.result.EndResult;
import org.sartframework.result.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class ConflictResolutionProjection extends KafkaTransactionProjection <ConflictResolvedResult>{

    final static Logger LOGGER = LoggerFactory.getLogger(ConflictResolutionProjection.class);

    private final KafkaWriters writeChannels;

    private final SartKafkaConfiguration kafkaStreamsConfiguration;

    private final ConflictResolutionRepository repository;

    final QueryResultsEmitter<ConflictResolvedResult> queryResultsEmitter;

    @Autowired
    public ConflictResolutionProjection(KafkaWriters writeChannels, SartKafkaConfiguration kafkaStreamsConfiguration,
                                        ConflictResolutionRepository repository) {
        super();
        this.writeChannels = writeChannels;
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.repository = repository;
        this.queryResultsEmitter = new KafkaTransactionQueryResultsEmitter<ConflictResolvedResult>(this);

    }

    @DomainEventHandler
    public void on(ConflictResolvedEvent e) {
        
        ConflictResolvedData d = e.getData();
        
        LOGGER.info("Conflict resolved event received for {}, persisting={}", d.getAggregateKey(), e.getXid() == d.getOtherXid());

        // policy only loser persisted as ConflictResolutionEntity
        if (e.getXid() == d.getOtherXid()) {
            
            ConflictResolutionEntity conflictResolvedEntity = new ConflictResolutionEntity(d.getAggregateKey(), d.getChangeKey(),
                d.getWinnerVersion(), d.getOtherVersion(), d.getWinnerXid(), d.getOtherXid(), d.getWinnerEvent(), d.getOtherEvent());
            
            repository.saveAndFlush(conflictResolvedEntity);

            ConflictResolvedResult result = new ConflictResolvedResult(kafkaStreamsConfiguration.getSid(), QueryResult.BROADCAST_RESULT_QUERY_KEY, d);

            queryResultsEmitter.broadcast(ConflictsByAggregateQuery.class, query -> query.matches(d.getAggregateKey()), result);
            queryResultsEmitter.broadcast(ConflictsByChangeQuery.class, query -> query.matches(d.getChangeKey()), result);
            queryResultsEmitter.broadcast(ConflictsByXidQuery.class, query -> query.matches(d.getWinnerXid(), d.getOtherXid()), result);
        }
    }

    @DomainQueryHandler
    public List<? super QueryResult> findByAggregateKey(ConflictsByAggregateQuery q) {

        return resultList(q, repository.findByAggregateKey(q.getAggregateKey()));
    }

    @DomainQueryHandler
    public List<? super QueryResult> findByChangeKey(ConflictsByChangeQuery q) {

        return resultList(q, repository.findByChangeKey(q.getChangeKey()));
    }

    @DomainQueryHandler
    public List<? super QueryResult> findByXid(ConflictsByXidQuery q) {

        return resultList(q, repository.findByXid(q.getXid()));
    }

    private List<? super QueryResult> resultList(AbstractQuery query, List<ConflictResolutionEntity> entityList) {

        return entityList.isEmpty() ? emptyResult(query) : nonEmptyResult(query, entityList);
    }

    protected List<? super QueryResult> nonEmptyResult(AbstractQuery query, List<ConflictResolutionEntity> entityList) {

        LOGGER.info("non-empty conflict result {}", entityList.size());

        List<? super QueryResult> resultList = entityList.stream()
            .map(entity -> new ConflictResolvedResult(kafkaStreamsConfiguration.getSid(), query.getQueryKey(), new ConflictResolvedData(entity.getOtherXid(), entity.getAggregateKey(), entity.getChangeKey(),
                entity.getWinnerVersion(), entity.getOtherVersion(), entity.getWinnerXid(), entity.getOtherXid(), entity.getWinnerEvent(),
                entity.getOtherEvent())))
            .collect(Collectors.toList());

        if (!query.isQuerySubscription()) {
            resultList.add(new EndResult());
        }
        return resultList;
    }

    private List<? super QueryResult> emptyResult(AbstractQuery query) {

        LOGGER.info("empty conflict result");

        return query.isQuerySubscription() ? new ArrayList<>(0) : Arrays.asList(new EmptyResult(kafkaStreamsConfiguration.getSid(), query.getQueryKey()));
    }

    @Override
    public String getEventTopic() {

        return kafkaStreamsConfiguration.getTransactionEventTopic();
    }

    @Override
    public String getQueryTopic() {

        return kafkaStreamsConfiguration.getConflictQueryTopic();
    }

    @Override
    public String getQueryEventTopic() {

        return kafkaStreamsConfiguration.getConflictQueryEventTopic();
    }

    @Override
    public String getQueryResultTopic() {

        return kafkaStreamsConfiguration.getConflictQueryResultTopic();
    }

    @Override
    public String getResultListenerServiceName() {

        return "conflict-result-listener-";
    }

    @Override
    public KafkaTemplate<String, DomainQuery> getQueryWriter() {

        return writeChannels.getConflictQueryWriter();
    }

    @Override
    public <E extends QueryEvent> KafkaTemplate<String, E> getQueryEventWriter() {

        return writeChannels.getConflictQueryEventWriter();
    }

    @Override
    public KafkaTemplate<String, ConflictResolvedResult> getQueryResultWriter() {

        return writeChannels.getConflictQueryResultWriter();
    }

}
