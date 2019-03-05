package org.sartframework.projection;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.sartframework.annotation.ArgumentTypeCollector;
import org.sartframework.annotation.AsynchHandlerDelegator;
import org.sartframework.annotation.DomainEventHandler;
import org.sartframework.annotation.DomainQueryHandler;
import org.sartframework.event.TransactionEvent;
import org.sartframework.query.DomainQuery;

public abstract class AnnotatedTransactionProjection implements TransactionProjection {

    final ArgumentTypeCollector<AnnotatedTransactionProjection, DomainEventHandler> eventTypes;

    final ArgumentTypeCollector<AnnotatedTransactionProjection, DomainQueryHandler> queryTypes;

    final Map<String, DomainQuery> subscriptions = new HashMap<>();

    public AnnotatedTransactionProjection() {
        super();
        this.eventTypes = ArgumentTypeCollector.<AnnotatedTransactionProjection, DomainEventHandler> wrap(this, DomainEventHandler.class);
        this.queryTypes = ArgumentTypeCollector.<AnnotatedTransactionProjection, DomainQueryHandler> wrap(this, DomainQueryHandler.class);

    }

    @Override
    public void handle(TransactionEvent transactionEvent) {

        AsynchHandlerDelegator.<TransactionEvent, DomainEventHandler> wrap(this, DomainEventHandler.class).handle(transactionEvent);
        
    }

    @Override
    public <Q extends DomainQuery, R> List<R> handleQuery(Q domainQuery) {

        if (domainQuery.isQuerySubscription()) {

            subscriptions.put(domainQuery.getQueryKey(), domainQuery);
        }

        return QueryDelegator.<DomainQueryHandler> wrap(this, DomainQueryHandler.class).handleQuery(domainQuery);
    }

    @Override
    public <E extends TransactionEvent> boolean hasEventType(Class<E> eventType) {

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
