package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.command.transaction.TransactionStatus;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;

@Evolvable(version = 1)
public class TransactionCompletedEvent extends GenericEvent implements TransactionEvent, TransactionStatus {

    int statusInternal;
    
    public TransactionCompletedEvent() {
        super();
    }

    public TransactionCompletedEvent(long xid, int status) {
        super(xid);
        this.statusInternal = status;
    }

    public int getStatusInternal() {
        return statusInternal;
    }

    public void setStatusInternal(int status) {
        this.statusInternal = status;
    }

    @Override
    public Status getStatus() {
        switch (getStatusInternal()) {
            case 1:
                return Status.CREATED;
            case 2:
                return Status.RUNNING;
            case 8:
                return Status.COMMITED;
            case 64:
                return Status.ABORTED;
            default:
                return Status.UNKNOWN;
        }
    }

}
