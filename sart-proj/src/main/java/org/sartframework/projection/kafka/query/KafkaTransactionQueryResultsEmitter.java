package org.sartframework.projection.kafka.query;

import java.util.function.Predicate;

import org.sartframework.query.DomainQuery;
import org.sartframework.query.QueryResultsEmitter;
import org.sartframework.result.QueryResult;

public class KafkaTransactionQueryResultsEmitter <R extends QueryResult>  implements QueryResultsEmitter <R> {

    private final KafkaTransactionProjection<R> projection;

    public KafkaTransactionQueryResultsEmitter(KafkaTransactionProjection<R> projection) {
        super();
        this.projection = projection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Q extends DomainQuery> void broadcast(Class<Q> queryType, Predicate<Q> filter, R queryResult) {

        projection.getSubscriptions().forEach((queryKey, domainQuery) -> {
            if (domainQuery.getClass().equals(queryType) && filter.test((Q) domainQuery)) {
                projection.getQueryResultWriter().sendDefault(queryKey, queryResult);
            }
        });
    }

}
