package org.sartframework.event.transaction;

import org.sartframework.annotation.Evolvable;
import org.sartframework.event.GenericEvent;
import org.sartframework.event.TransactionEvent;
import org.sartframework.session.SystemSnapshot;

@Evolvable(version = 1)
public class TransactionStartedEvent extends GenericEvent implements TransactionEvent {

    SystemSnapshot systemSnapshot;
    
    int isolation;

    public TransactionStartedEvent() {
        super();
    }

    public TransactionStartedEvent(long xid, int isolation, SystemSnapshot systemSnapshot) {
        super(xid);
        this.systemSnapshot = systemSnapshot;
        this.isolation = isolation;
    }

    public int getIsolation() {
        return isolation;
    }

    public void setIsolation(int isolation) {
        this.isolation = isolation;
    }

    public SystemSnapshot getSystemSnapshot() {
        return systemSnapshot;
    }

    public void setSystemSnapshot(SystemSnapshot systemSnapshot) {
        this.systemSnapshot = systemSnapshot;
    }

}
