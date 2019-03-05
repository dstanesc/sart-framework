package org.sartframework.query;

import java.util.function.Predicate;

import org.sartframework.result.QueryResult;

public interface QueryResultsEmitter {

    <Q extends DomainQuery, U extends QueryResult> void  broadcast(Class<Q> queryType, Predicate<? super Q> filter, U update);
}
