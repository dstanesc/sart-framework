package org.sartframework.event.transaction;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

public class ProgressLoggedEvent extends GenericEvent implements TransactionEvent {

    long xcs;

    DomainEvent<? extends DomainCommand> domainEvent;

    public ProgressLoggedEvent() {
        super();
    }

    public ProgressLoggedEvent(long xid, long xcs, DomainEvent<? extends DomainCommand> domainEvent) {
        super(xid);
        this.xcs = xcs;
        this.domainEvent = domainEvent;
    }

    public long getXcs() {
        return xcs;
    }

    public void setXcs(long xcs) {
        this.xcs = xcs;
    }

    public DomainEvent<? extends DomainCommand> getDomainEvent() {
        return domainEvent;
    }

    public void setDomainEvent(DomainEvent<? extends DomainCommand> event) {
        this.domainEvent = event;
    }

}
