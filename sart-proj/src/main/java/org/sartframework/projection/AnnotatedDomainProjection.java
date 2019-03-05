package org.sartframework.projection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sartframework.annotation.ArgumentTypeCollector;
import org.sartframework.annotation.DomainEventHandler;
import org.sartframework.annotation.DomainQueryHandler;
import org.sartframework.annotation.SynchHandlerDelegator;
import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.query.DomainQuery;

public abstract class AnnotatedDomainProjection implements DomainProjection {

    final ArgumentTypeCollector<AnnotatedDomainProjection, DomainEventHandler> eventTypes;

    final ArgumentTypeCollector<AnnotatedDomainProjection, DomainQueryHandler> queryTypes;

    final Map<String, DomainQuery> subscriptions = new HashMap<>();
    
    public AnnotatedDomainProjection() {
        super();
        eventTypes = ArgumentTypeCollector.<AnnotatedDomainProjection, DomainEventHandler> wrap(this, DomainEventHandler.class);
        queryTypes = ArgumentTypeCollector.<AnnotatedDomainProjection, DomainQueryHandler> wrap(this, DomainQueryHandler.class);
    }

    @Override
    public Long handle(DomainEvent<? extends DomainCommand> domainEvent) {

        return SynchHandlerDelegator.<DomainEvent<? extends DomainCommand>, DomainEventHandler, Long> wrap(this, DomainEventHandler.class).handle(domainEvent);
    }

    @Override
    public <Q extends DomainQuery, R> List<R> handleQuery(Q domainQuery) {

        if (domainQuery.isQuerySubscription()) {
            
            subscriptions.put(domainQuery.getQueryKey(), domainQuery);
        }
        
        return QueryDelegator.<DomainQueryHandler> wrap(this, DomainQueryHandler.class).handleQuery(domainQuery);
    }

    @Override
    public <E extends  DomainEvent<?>> boolean hasEventType(Class<E> eventType) {

        return eventTypes.hasParameterType(eventType);
    }

    @Override
    public <Q extends DomainQuery> boolean hasQueryType(Class<Q> queryType) {

        return queryTypes.hasParameterType(queryType);
    }

    @Override
    public String getName() {
       
        return getClass().getName();
    }
    
    @Override
    public void unsubscribe(String queryKey) {

        subscriptions.remove(queryKey);
    }

    public Map<String, DomainQuery> getSubscriptions() {
        return subscriptions;
    }
    
}
