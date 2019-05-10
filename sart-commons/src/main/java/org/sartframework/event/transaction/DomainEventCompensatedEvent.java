package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.DomainCommand;
import org.sartframework.event.DomainEvent;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

@Evolvable(version = 1)
public class DomainEventCompensatedEvent extends GenericEvent implements TransactionEvent {

    DomainEvent<? extends DomainCommand> domainEvent;
    
    boolean skip;

    public DomainEventCompensatedEvent() {
        super();
    }

    public DomainEventCompensatedEvent(long xid, long xcs, DomainEvent<? extends DomainCommand> domainEvent, boolean skip) {
        super(xid);
        this.domainEvent = domainEvent;
        this.skip = skip;
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

}
