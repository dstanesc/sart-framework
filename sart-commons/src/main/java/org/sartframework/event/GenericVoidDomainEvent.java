package org.sartframework.event;

import org.sartframework.command.GenericVoidDomainCommand;
import org.sartframework.command.VoidDomainCommand;

public class GenericVoidDomainEvent extends GenericDomainEvent<VoidDomainCommand> implements VoidDomainEvent {


    public GenericVoidDomainEvent() {
        super();
    }

    public GenericVoidDomainEvent(String aggregateKey, long sourceAggregateVersion) {
        super(aggregateKey, sourceAggregateVersion);
    }

    @Override
    public VoidDomainCommand undo(long xid, long xcs) {
       // should we  throw new UnsupportedOperationException() ?
        return new GenericVoidDomainCommand(getAggregateKey(), getSourceAggregateVersion());
    }

    @Override
    public String getChangeKey() {
        // should we  throw new UnsupportedOperationException() ?
        return getAggregateKey();
    }

   
}
