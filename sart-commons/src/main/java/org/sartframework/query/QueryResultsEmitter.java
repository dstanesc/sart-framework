package org.sartframework.query;

import java.util.function.Predicate;

import org.sartframework.result.QueryResult;

public interface QueryResultsEmitter <R extends QueryResult> {

    <Q extends DomainQuery> void  broadcast(Class<Q> queryType, Predicate<Q> filter, R queryResult);
}
