package org.sartframework.projection.kafka.query;

import javax.annotation.PostConstruct;

import org.sartframework.event.query.QueryUnsubscribedEvent;
import org.sartframework.kafka.config.SartKafkaConfiguration;
import org.sartframework.projection.kafka.services.QueryResultListenerService;
import org.sartframework.query.DomainQuery;
import org.sartframework.query.QueryManager;
import org.sartframework.result.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.publisher.Flux;

public class KafkaTransactionQueryManager implements QueryManager {

    final static Logger LOGGER = LoggerFactory.getLogger(KafkaTransactionQueryManager.class);

    private static QueryManager instance;

    final private SartKafkaConfiguration kafkaStreamsConfiguration;

    final KafkaTransactionProjection<? extends QueryResult> transactionProjection;

    public KafkaTransactionQueryManager(SartKafkaConfiguration kafkaConfiguration, KafkaTransactionProjection<? extends QueryResult> transactionProjection) {
        super();
        this.kafkaStreamsConfiguration = kafkaConfiguration;
        this.transactionProjection = transactionProjection;
    }

    @PostConstruct
    public QueryManager init() {

        instance = this;

        return this;
    }

    public static QueryManager get() {

        return instance;
    }

    @Override
    public <Q extends DomainQuery, T> Flux<T> query(Q domainQuery, Class<T> resultType) {

        String queryKey = domainQuery.getQueryKey();

        // create stream before publishing the query
        QueryResultListenerService<T> resultListenerService = new QueryResultListenerService<T>(kafkaStreamsConfiguration, transactionProjection, domainQuery).start();

        transactionProjection.getQueryWriter().sendDefault(queryKey, domainQuery);

        return resultListenerService.getResultPublisher().doAfterTerminate(() -> {

            transactionProjection.getQueryEventWriter().sendDefault(queryKey, new QueryUnsubscribedEvent(queryKey));

            resultListenerService.stop();
        });
    }

}
