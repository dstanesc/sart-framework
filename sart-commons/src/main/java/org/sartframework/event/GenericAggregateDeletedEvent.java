package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateDeletedEvent<C extends DomainCommand> extends GenericDomainEvent<C> implements AggregateDestructedEvent<C> {

    public GenericAggregateDeletedEvent() {
        super();
    }

    public GenericAggregateDeletedEvent(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
