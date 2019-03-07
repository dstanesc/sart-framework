package org.sartframework.projection;

import org.sartframework.aggregate.AsynchHandler;
import org.sartframework.event.TransactionEvent;
import org.sartframework.query.DomainQuery;
import org.sartframework.result.QueryResult;

public interface TransactionProjection <R extends QueryResult> extends AsynchHandler<TransactionEvent>, QueryHandler<R>, ProjectionConfiguration {
    
    String getName();

    <E extends TransactionEvent> boolean hasEventType(Class<E> eventType);
    
    <Q extends DomainQuery> boolean hasQueryType(Class<Q> queryType);

    <Q extends DomainQuery> void unsubscribe(String queryKey);

}
