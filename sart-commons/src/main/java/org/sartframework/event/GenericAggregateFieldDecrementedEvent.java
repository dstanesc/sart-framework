package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateFieldDecrementedEvent<C extends DomainCommand> extends GenericDomainEvent<C>
    implements AggregateFieldDecrementedEvent<C> {

    public GenericAggregateFieldDecrementedEvent() {
        super();
    }

    public GenericAggregateFieldDecrementedEvent(String aggregateKey, long aggregateVersion) {
        super(aggregateKey, aggregateVersion);
    }

}
