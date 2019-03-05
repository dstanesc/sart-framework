package org.sartframework.event.transaction;

import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

public class TransactionCommittedEvent extends GenericEvent implements TransactionEvent {

    public TransactionCommittedEvent() {
        super();
    }

    public TransactionCommittedEvent(long xid) {
        super(xid);
    }
}
