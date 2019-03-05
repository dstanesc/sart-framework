package org.sartframework.projection;

import org.sartframework.aggregate.SynchHandler;
import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.query.DomainQuery;

public interface DomainProjection extends SynchHandler<DomainEvent<? extends DomainCommand>, Long>, QueryHandler, ProjectionConfiguration {
    
    String getName();

    <E extends DomainEvent<?>> boolean hasEventType(Class<E> eventType);
    
    <Q extends DomainQuery> boolean hasQueryType(Class<Q> queryType);
    
    <Q extends DomainQuery> void unsubscribe(String queryKey);

}
