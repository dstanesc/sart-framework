package org.sartframework.command.transaction;

import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;

public class LogProgressCommand implements TransactionCommand {

    long xid;

    long xcs;

    DomainEvent<? extends DomainCommand> domainEvent;

    public LogProgressCommand() {
        super();
    }

    public LogProgressCommand(long xid, long xcs, DomainEvent<? extends DomainCommand> domainEvent) {
        super();
        this.xid = xid;
        this.xcs = xcs;
        this.domainEvent = domainEvent;
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
}
