package org.sartframework.event.transaction;

import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

public class TransactionAbortedEvent extends GenericEvent implements TransactionEvent {

    public TransactionAbortedEvent() {
        super();
    }

    public TransactionAbortedEvent(long xid) {
        super(xid);
    }
}
