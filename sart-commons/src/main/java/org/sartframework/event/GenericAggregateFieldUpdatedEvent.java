package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateFieldUpdatedEvent<C extends DomainCommand> extends GenericDomainEvent<C>
    implements AggregateFieldUpdatedEvent<C> {

    public GenericAggregateFieldUpdatedEvent() {
        super();
    }

    public GenericAggregateFieldUpdatedEvent(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);

    }
}
