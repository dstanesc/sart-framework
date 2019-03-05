package org.sartframework.event;

import org.sartframework.command.DomainCommand;

public abstract class GenericAggregateFieldElementAddedEvent<C extends DomainCommand> extends GenericDomainEvent<C>
    implements AggregateFieldElementAddedEvent<C> {

    String addedElementKey;

    public GenericAggregateFieldElementAddedEvent() {
        super();
    }

    public GenericAggregateFieldElementAddedEvent(String aggregateKey, long aggregateVersion, String addedElementKey) {
        super(aggregateKey, aggregateVersion);
        this.addedElementKey = addedElementKey;
    }

    public String getAddedElementKey() {
        return addedElementKey;
    }

    public void setAddedElementKey(String addedElementKey) {
        this.addedElementKey = addedElementKey;
    }

}
