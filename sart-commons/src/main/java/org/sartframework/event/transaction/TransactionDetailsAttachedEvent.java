package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.transaction.TransactionDetails;

@Evolvable(version = 1)
public class TransactionDetailsAttachedEvent extends GenericEvent implements TransactionEvent {

    TransactionDetails details = new TransactionDetails();

    public TransactionDetailsAttachedEvent() {
        super();
    }

    public TransactionDetailsAttachedEvent(long xid, TransactionDetails details) {
        super(xid);
        this.details = details;
    }

    public TransactionDetails getDetails() {
        return details;
    }
}
