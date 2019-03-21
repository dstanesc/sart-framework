package org.sartframework.command.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;

@Evolvable(version = 1)
public class CompensateDomainEventCommand implements TransactionCommand {
    
    long xid;

    long xcs;

    DomainEvent<? extends DomainCommand> domainEvent;
    
    boolean skip;

    public CompensateDomainEventCommand() {
        super();
    }

    public CompensateDomainEventCommand(long xid, long xcs, DomainEvent<? extends DomainCommand> domainEvent,  boolean skip) {
        super();
        this.xid = xid;
        this.xcs = xcs;
        this.domainEvent = domainEvent;
        this.skip = skip;
    }

    @Override
    public long getXid() {
        return xid;
    }

    public void setXid(long xid) {
        this.xid = xid;
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

    public boolean isSkip() {
        return skip;
    }

    public void setSkip(boolean skip) {
        this.skip = skip;
    }

    @Override
    public String toString() {
        return "CompensateDomainEventCommand [xid=" + xid + ", xcs=" + xcs + ", domainEvent=" + domainEvent + ", skip=" + skip + "]";
    }

}
