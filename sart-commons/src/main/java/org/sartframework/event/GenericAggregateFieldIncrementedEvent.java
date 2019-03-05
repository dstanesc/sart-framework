package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateFieldIncrementedEvent<C extends DomainCommand> extends GenericDomainEvent<C>
    implements AggregateFieldIncrementedEvent<C> {

    public GenericAggregateFieldIncrementedEvent() {
        super();
    }

    public GenericAggregateFieldIncrementedEvent(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
