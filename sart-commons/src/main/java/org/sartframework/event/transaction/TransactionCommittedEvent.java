package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

@Evolvable(version = 1)
public class TransactionCommittedEvent extends GenericEvent implements TransactionEvent {

    public TransactionCommittedEvent() {
        super();
    }

    public TransactionCommittedEvent(long xid) {
        super(xid);
    }
}
