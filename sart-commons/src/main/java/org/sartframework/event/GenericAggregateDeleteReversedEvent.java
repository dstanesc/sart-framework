package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateDeleteReversedEvent<C extends DomainCommand> extends GenericDomainEvent<C>
    implements AggregateDestructionReversedEvent<C> {

    public GenericAggregateDeleteReversedEvent() {
        super();
    }

    public GenericAggregateDeleteReversedEvent(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
