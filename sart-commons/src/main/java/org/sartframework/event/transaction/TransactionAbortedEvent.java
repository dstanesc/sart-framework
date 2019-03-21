package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

@Evolvable(version = 1)
public class TransactionAbortedEvent extends GenericEvent implements TransactionEvent {

    public TransactionAbortedEvent() {
        super();
    }

    public TransactionAbortedEvent(long xid) {
        super(xid);
    }
}
