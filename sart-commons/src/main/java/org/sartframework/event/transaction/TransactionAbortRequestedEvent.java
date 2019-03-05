package org.sartframework.event.transaction;

import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

public class TransactionAbortRequestedEvent extends GenericEvent implements TransactionEvent {

    public TransactionAbortRequestedEvent() {
        super();
    }

    public TransactionAbortRequestedEvent(long xid) {
        super(xid);
    }
}
