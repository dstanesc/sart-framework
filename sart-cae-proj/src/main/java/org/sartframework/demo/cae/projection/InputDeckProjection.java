package org.sartframework.demo.cae.projection;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.SortedSet;
import java.util.stream.Collectors;

import javax.transaction.Transactional;

import org.sartframework.annotation.DomainEventHandler;
import org.sartframework.annotation.DomainQueryHandler;
import org.sartframework.demo.cae.event.InputDeckCreatedEvent;
import org.sartframework.demo.cae.event.InputDeckFileUpdatedEvent;
import org.sartframework.demo.cae.event.InputDeckResultAddedEvent;
import org.sartframework.demo.cae.event.InputDeckResultRemovedEvent;
import org.sartframework.demo.cae.query.InputDeckByIdQuery;
import org.sartframework.demo.cae.query.InputDeckByNameQuery;
import org.sartframework.demo.cae.query.InputDeckByXidQuery;
import org.sartframework.demo.cae.result.InputDeckQueryResult;
import org.sartframework.event.QueryEvent;
import org.sartframework.kafka.channels.KafkaWriters;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.projection.ProjectedEntity;
import org.sartframework.projection.EntityIdentity;
import org.sartframework.projection.kafka.query.KafkaDomainProjection;
import org.sartframework.projection.kafka.query.KafkaDomainQueryResultsEmitter;
import org.sartframework.query.AbstractQuery;
import org.sartframework.query.DomainQuery;
import org.sartframework.query.QueryResultsEmitter;
import org.sartframework.result.EmptyResult;
import org.sartframework.result.EndResult;
import org.sartframework.result.QueryResult;
import org.sartframework.session.SystemSnapshot;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class InputDeckProjection extends KafkaDomainProjection {

    final static Logger LOGGER = LoggerFactory.getLogger(InputDeckProjection.class);

    private final KafkaWriters writeChannels;

    private final SartKafkaConfiguration kafkaStreamsConfiguration;

    private final InputDeckRepository inputDeckRepository;

    private final QueryResultsEmitter queryResultsEmitter;

    public InputDeckProjection(KafkaWriters writeChannels, SartKafkaConfiguration kafkaStreamsConfiguration,
                               InputDeckRepository inputDeckRepository) {
        super();
        this.writeChannels = writeChannels;
        this.kafkaStreamsConfiguration = kafkaStreamsConfiguration;
        this.inputDeckRepository = inputDeckRepository;
        this.queryResultsEmitter = new KafkaDomainQueryResultsEmitter(this);
    }

    @DomainEventHandler
    @Transactional
    public void on(InputDeckCreatedEvent inputDeckCreatedEvent) {

        LOGGER.info("inputDeckId={}", inputDeckCreatedEvent.getAggregateKey());

        InputDeckEntity inputDeckEntity = new InputDeckEntity(inputDeckCreatedEvent.getXid(), inputDeckCreatedEvent.getAggregateKey().toString(),
            inputDeckCreatedEvent.getSourceAggregateVersion(), inputDeckCreatedEvent.getInputDeckName(), inputDeckCreatedEvent.getInputDeckFile());

        inputDeckRepository.saveAndFlush(inputDeckEntity);

        emit(inputDeckEntity);
    }

    @DomainEventHandler
    @Transactional
    public void on(InputDeckResultAddedEvent resultAddedEvent) {

        LOGGER.info("targetVersion={} inputDeckId={} inputDeckVersion={} resultId={} resultFile={}. Saving.\n",
            resultAddedEvent.getTargetAggregateVersion(), resultAddedEvent.getAggregateKey(), resultAddedEvent.getSourceAggregateVersion(),
            resultAddedEvent.getAddedElementKey(), resultAddedEvent.getResultFile());

        EntityIdentity inputDeckIdentity = new EntityIdentity(resultAddedEvent.getAggregateKey().toString(),
            resultAddedEvent.getSourceAggregateVersion());

        Optional<InputDeckEntity> inputDeckEntityOptional = inputDeckRepository.findById(inputDeckIdentity);

        if (inputDeckEntityOptional.isPresent()) {

            InputDeckEntity oldInputDeckEntityVersion = inputDeckEntityOptional.get();
            oldInputDeckEntityVersion.setXmax(resultAddedEvent.getXid());

            InputDeckEntity newInputDeckEntityVersion = oldInputDeckEntityVersion.copy(resultAddedEvent.getXid(),
                resultAddedEvent.getTargetAggregateVersion());
            newInputDeckEntityVersion.addResult(new ResultEntity(resultAddedEvent.getXid(), resultAddedEvent.getAddedElementKey(),
                resultAddedEvent.getResultName(), resultAddedEvent.getResultFile()));

            inputDeckRepository.saveAndFlush(oldInputDeckEntityVersion);
            inputDeckRepository.saveAndFlush(newInputDeckEntityVersion);

            emit(newInputDeckEntityVersion);

        } else
            throw new IllegalStateException("could not find aggregate, InputDeckProjection corrupted");
    }

    @DomainEventHandler
    @Transactional
    public void on(InputDeckResultRemovedEvent resultRemovedEvent) {

        LOGGER.info("targetVersion={} inputDeckId={} inputDeckVersion={} resultId={} Saving.\n", resultRemovedEvent.getTargetAggregateVersion(),
            resultRemovedEvent.getAggregateKey(), resultRemovedEvent.getSourceAggregateVersion(), resultRemovedEvent.getRemovedElementKey());

        EntityIdentity inputDeckIdentity = new EntityIdentity(resultRemovedEvent.getAggregateKey().toString(),
            resultRemovedEvent.getSourceAggregateVersion());

        Optional<InputDeckEntity> inputDeckEntityOptional = inputDeckRepository.findById(inputDeckIdentity);

        if (inputDeckEntityOptional.isPresent()) {
            InputDeckEntity oldInputDeckEntityVersion = inputDeckEntityOptional.get();
            oldInputDeckEntityVersion.setXmax(resultRemovedEvent.getXid());

            InputDeckEntity newInputDeckEntityVersion = oldInputDeckEntityVersion.copy(resultRemovedEvent.getXid(),
                resultRemovedEvent.getTargetAggregateVersion());
            newInputDeckEntityVersion.removeResult(resultRemovedEvent.getRemovedElementKey(), resultRemovedEvent.getXid());

            inputDeckRepository.saveAndFlush(oldInputDeckEntityVersion);
            inputDeckRepository.saveAndFlush(newInputDeckEntityVersion);

            emit(newInputDeckEntityVersion);

        } else
            throw new IllegalStateException("could not find aggregate, InputDeckProjection corrupted");
    }

    @DomainEventHandler
    @Transactional
    public void on(InputDeckFileUpdatedEvent fileUpdatedEvent) {

        LOGGER.info("targetVersion={} inputDeckId={} inputDeckVersion={} inputDeckFile={}. Saving.\n", fileUpdatedEvent.getTargetAggregateVersion(),
            fileUpdatedEvent.getAggregateKey(), fileUpdatedEvent.getSourceAggregateVersion(), fileUpdatedEvent.getInputDeckFile());

        EntityIdentity inputDeckIdentity = new EntityIdentity(fileUpdatedEvent.getAggregateKey().toString(),
            fileUpdatedEvent.getSourceAggregateVersion());

        Optional<InputDeckEntity> inputDeckEntityOptional = inputDeckRepository.findById(inputDeckIdentity);

        if (inputDeckEntityOptional.isPresent()) {

            InputDeckEntity oldInputDeckEntityVersion = inputDeckEntityOptional.get();
            oldInputDeckEntityVersion.setXmax(fileUpdatedEvent.getXid());

            InputDeckEntity newInputDeckEntityVersion = oldInputDeckEntityVersion.copy(fileUpdatedEvent.getXid(),
                fileUpdatedEvent.getTargetAggregateVersion());
            newInputDeckEntityVersion.setInputDeckFile(fileUpdatedEvent.getInputDeckFile());

            inputDeckRepository.saveAndFlush(oldInputDeckEntityVersion);
            inputDeckRepository.saveAndFlush(newInputDeckEntityVersion);

            emit(newInputDeckEntityVersion);

        } else
            throw new IllegalStateException("could not find aggregate, InputDeckProjection corrupted");

    }

    protected void emit(InputDeckEntity inputDeckEntity) {

        InputDeckQueryResult result = new InputDeckQueryResult(QueryResult.BROADCAST_RESULT_QUERY_KEY, inputDeckEntity.getXmin(),
            inputDeckEntity.getAggregateKey(), inputDeckEntity.getAggregateVersion(), inputDeckEntity.getEntityCreationTime(), 
            inputDeckEntity.getInputDeckName(), inputDeckEntity.getInputDeckFile());

        LOGGER.info("Emit  InputDeckQueryResult aggregateVersion={}, aggregateKey={}", result.getInputDeckVersion(), result.getInputDeckId());

        queryResultsEmitter.broadcast(InputDeckByIdQuery.class, query -> query.matches(result.getInputDeckId()), result);
        queryResultsEmitter.broadcast(InputDeckByXidQuery.class, query -> query.matches(result.getXid()), result);
        queryResultsEmitter.broadcast(InputDeckByNameQuery.class, query -> query.matches(result.getInputDeckName()), result);
    }

    @DomainQueryHandler
    public List<? super QueryResult> findByInputDeckId(InputDeckByIdQuery query) {

        LOGGER.info("Handling InputDeckByIdQuery {}", query.getInputDeckId());

        List<InputDeckEntity> entityList = inputDeckRepository.findByAggregateKey(query.getInputDeckId());

        LOGGER.info("InputDeckByIdQuery existing {}", entityList.size());

        List<InputDeckEntity> visibleList = filterVisibility(query, entityList);

        LOGGER.info("InputDeckByIdQuery visible {}", visibleList.size());

        return resultList(query, visibleList);
    }

    @DomainQueryHandler
    public List<? super QueryResult> findByXmin(InputDeckByXidQuery query) {

        LOGGER.info("Handling InputDeckByXidQuery {}", query.getXid());

        List<InputDeckEntity> entityList = inputDeckRepository.findByXmin(query.getXid());

        LOGGER.info("InputDeckByXidQuery results {}", entityList.size());

        List<InputDeckEntity> visibleList = filterVisibility(query, entityList);

        LOGGER.info("InputDeckByXidQuery visible {}", visibleList.size());

        return resultList(query, visibleList);
    }

    @DomainQueryHandler
    public List<? super QueryResult> findByInputDeckName(InputDeckByNameQuery query) {

        LOGGER.info("Handling InputDeckByNameQuery {}", query.getInputDeckName());

        List<InputDeckEntity> entityList = inputDeckRepository.findByInputDeckName(query.getInputDeckName());

        LOGGER.info("InputDeckByNameQuery results {}", entityList.size());

        List<InputDeckEntity> visibleList = filterVisibility(query, entityList);

        LOGGER.info("InputDeckByNameQuery visible {}", visibleList.size());

        return resultList(query, visibleList);
    }

    private List<InputDeckEntity> filterVisibility(DomainQuery domainQuery, List<InputDeckEntity> entityList) {
        // xmin <= highest && xmin not in [x1, x2, ...xk (running transactions)]
        // && [xmax == null || xmax > xid]
       
        int isolation = domainQuery.getIsolation();
        
        //FIXME handle properly all isolation levels 1 - READ_UNCOMMITTED, 2- READ_COMMITTED, 4- READ_SNAPSHOT
        if(isolation == 1) return entityList;

        SystemSnapshot systemSnapshot = domainQuery.getSystemSnapshot();
        
        if(systemSnapshot.isEmpty()) return  entityList;

        Long highestCommitted = systemSnapshot.getHighestCommitted();

        SortedSet<Long> running = systemSnapshot.getRunning();

        long xid = domainQuery.getQueryXid();

        return entityList.stream().filter(entity -> {

            try {
                long xmin = entity.getXmin();

                long xmax = entity.getXmax();

                boolean filtered = xmin == xid || (xmin <= highestCommitted && !running.contains(xmin) && (xmax == ProjectedEntity.XMAX_NOT_SET || xmax > xid));

                LOGGER.info(
                    "Returned = {} :  xmin {} == xid {} || (xmin {} <= highestCommitted {} AND  xmin {} /= running transactions {} AND (xmax {} == NOT SET OR xmax {} > xid {})) ",
                    filtered, xmin, xid, xmin, highestCommitted, xmin, running, xmax, xmax, xid);

                return filtered;
                
            } catch (Exception e) {
                
              throw new RuntimeException();
            }
            
        }).collect(Collectors.toList());
    }

    private List<? super QueryResult> resultList(AbstractQuery query, List<InputDeckEntity> entityList) {
        return entityList.isEmpty() ? emptyResult(query) : nonEmptyResult(query, entityList);
    }

    private List<? super QueryResult> emptyResult(AbstractQuery query) {
        return query.isQuerySubscription() ? new ArrayList<>(0) : Arrays.asList(new EmptyResult(query.getQueryKey()));
    }

    protected List<? super QueryResult> nonEmptyResult(AbstractQuery query, List<InputDeckEntity> entityList) {

        List<? super QueryResult> resultList = entityList.stream().map(e -> {

            LOGGER.info("Return InputDeckQueryResult aggregateVersion={} aggregateKey={}", e.getAggregateVersion(), e.getAggregateKey());

            return new InputDeckQueryResult(query.getQueryKey(), e.getXmin(), e.getAggregateKey(), e.getAggregateVersion(), e.getEntityCreationTime(), e.getInputDeckName(),
                e.getInputDeckFile());

        }).collect(Collectors.toList());

        if (!query.isQuerySubscription()) {
            resultList.add(new EndResult());
        }

        return resultList;
    }

    @Override
    public String getEventTopic() {

        return kafkaStreamsConfiguration.getDomainEventTopic();
    }

    @Override
    public String getQueryTopic() {

        return kafkaStreamsConfiguration.getDomainQueryTopic();
    }

    @Override
    public String getQueryResultTopic() {

        return kafkaStreamsConfiguration.getDomainQueryResultTopic();
    }

    @Override
    public String getQueryEventTopic() {

        return kafkaStreamsConfiguration.getDomainQueryEventTopic();
    }

    @Override
    public String getResultListenerServiceName() {

        return "inputDeck-result-listener-";
    }

    @Override
    public KafkaTemplate<String, DomainQuery> getQueryWriter() {

        return writeChannels.domainQueryWriter();
    }

    @Override
    public <R extends QueryResult> KafkaTemplate<String, R> getQueryResultWriter() {

        return writeChannels.domainQueryResultWriter();
    }

    @Override
    public <E extends QueryEvent> KafkaTemplate<String, E> getQueryEventWriter() {

        return writeChannels.domainQueryEventWriter();
    }

}
