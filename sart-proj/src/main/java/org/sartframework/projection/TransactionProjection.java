package org.sartframework.projection;

import org.sartframework.aggregate.AsynchHandler;
import org.sartframework.event.TransactionEvent;
import org.sartframework.query.DomainQuery;

public interface TransactionProjection extends AsynchHandler<TransactionEvent>, QueryHandler, ProjectionConfiguration {
    
    String getName();

    <E extends TransactionEvent> boolean hasEventType(Class<E> eventType);
    
    <Q extends DomainQuery> boolean hasQueryType(Class<Q> queryType);

    <Q extends DomainQuery> void unsubscribe(String queryKey);

}
