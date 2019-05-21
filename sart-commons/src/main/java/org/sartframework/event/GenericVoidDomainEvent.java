package org.sartframework.event;

import org.sartframework.command.DefaultVoidDomainCommand;
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
        return new DefaultVoidDomainCommand(getAggregateKey(), getSourceAggregateVersion()).addTransactionHeader(xid, xcs);
    }

    @Override
    public String getChangeKey() {
        return getAggregateKey();
    }

}
