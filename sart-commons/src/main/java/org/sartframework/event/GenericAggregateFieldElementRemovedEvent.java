package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateFieldElementRemovedEvent<C extends DomainCommand> extends GenericDomainEvent<C>
    implements AggregateFieldElementRemovedEvent<C> {

    String removedElementKey;

    public GenericAggregateFieldElementRemovedEvent() {
        super();
    }

    public GenericAggregateFieldElementRemovedEvent(String aggregateKey, long aggregateVersion, String removedElementIdentity) {
        super(aggregateKey, aggregateVersion);
        this.removedElementKey = removedElementIdentity;
    }

    public String getRemovedElementKey() {
        return removedElementKey;
    }

    public void setRemovedElementKey(String elementIdentity) {
        this.removedElementKey = elementIdentity;
    }

}
