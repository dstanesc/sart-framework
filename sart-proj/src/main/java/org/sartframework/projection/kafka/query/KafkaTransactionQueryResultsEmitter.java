package org.sartframework.projection.kafka.query;

import java.util.function.Predicate;

import org.sartframework.query.DomainQuery;
import org.sartframework.query.QueryResultsEmitter;
import org.sartframework.result.QueryResult;

public class KafkaTransactionQueryResultsEmitter implements QueryResultsEmitter {

    private final KafkaTransactionProjection projection;

    public KafkaTransactionQueryResultsEmitter(KafkaTransactionProjection projection) {
        super();
        this.projection = projection;
    }

    @Override
    public <Q extends DomainQuery, R extends QueryResult> void broadcast(Class<Q> queryType, Predicate<? super Q> filter, R queryResult) {

        projection.getSubscriptions().forEach((queryKey, domainQuery) -> {
            if (domainQuery.getClass().equals(queryType) && filter.test((Q) domainQuery)) {
                projection.getQueryResultWriter().sendDefault(queryKey, queryResult);
            }
        });
    }

}
