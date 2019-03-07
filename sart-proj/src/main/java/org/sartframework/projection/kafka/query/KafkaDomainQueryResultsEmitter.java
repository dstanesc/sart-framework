package org.sartframework.projection.kafka.query;

import java.util.function.Predicate;

import org.sartframework.query.DomainQuery;
import org.sartframework.query.QueryResultsEmitter;
import org.sartframework.result.QueryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KafkaDomainQueryResultsEmitter <R extends QueryResult> implements QueryResultsEmitter <R> {

    final static Logger LOGGER = LoggerFactory.getLogger(KafkaDomainQueryResultsEmitter.class);

    private final KafkaDomainProjection<?,R> projection;

    public KafkaDomainQueryResultsEmitter(KafkaDomainProjection<?,R> projection) {
        super();
        this.projection = projection;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <Q extends DomainQuery> void broadcast(Class<Q> queryType, Predicate<Q> filter, R queryResult) {

        projection.getSubscriptions().forEach((queryKey, domainQuery) -> {
           // LOGGER.info("Filtering query result queryClass={} for queryType={}, test={}", domainQuery.getClass(), queryType, filter.test((Q) domainQuery));
            if (domainQuery.getClass().equals(queryType) && filter.test((Q) domainQuery)) {
                LOGGER.info("Emitting query result {} for {}", queryResult, queryKey);
                projection.getQueryResultWriter().sendDefault(queryKey, queryResult);
            }
        });
    }
}
