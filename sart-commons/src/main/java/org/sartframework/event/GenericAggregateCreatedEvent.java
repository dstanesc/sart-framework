package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateCreatedEvent<C extends DomainCommand> extends GenericDomainEvent<C> implements AggregateCreatedEvent<C> {

    public GenericAggregateCreatedEvent() {
        super();
    }

    public GenericAggregateCreatedEvent(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
