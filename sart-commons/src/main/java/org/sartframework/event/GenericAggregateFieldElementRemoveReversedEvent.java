package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateFieldElementRemoveReversedEvent<C extends DomainCommand> extends GenericDomainEvent<C>
    implements AggregateFieldElementRemoveReversedEvent<C> {

    String removedElementKey;

    public GenericAggregateFieldElementRemoveReversedEvent() {
        super();
    }

    public GenericAggregateFieldElementRemoveReversedEvent(String aggregateKey, long aggregateVersion, String removedElementIdentity) {
        super(aggregateKey, aggregateVersion);
        this.removedElementKey = removedElementIdentity;
    }

    public String getRemovedElementKey() {
        return removedElementKey;
    }

    public void setRemovedElementKey(String removedElementIdentity) {
        this.removedElementKey = removedElementIdentity;
    }

}
