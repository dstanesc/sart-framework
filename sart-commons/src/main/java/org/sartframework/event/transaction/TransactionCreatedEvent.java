package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

@Evolvable(version = 1)
public class TransactionCreatedEvent extends GenericEvent implements TransactionEvent {


    public TransactionCreatedEvent() {
        super();
    }

    public TransactionCreatedEvent(long xid) {
        super(xid);

    }
}
