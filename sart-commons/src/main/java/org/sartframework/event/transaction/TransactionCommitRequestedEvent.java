package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

@Evolvable(version = 1)
public class TransactionCommitRequestedEvent extends GenericEvent implements TransactionEvent {

    long xct;

    public TransactionCommitRequestedEvent() {
        super();
    }

    public TransactionCommitRequestedEvent(long xid, long xct) {
        super(xid);
        this.xct = xct;
    }

    public long getXct() {
        return xct;
    }

    public void setXct(long xct) {
        this.xct = xct;
    }

}
