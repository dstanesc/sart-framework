package org.sartframework.error.transaction;

import org.sartframework.annotation.Evolvable;

@Evolvable(version = 1)
public final class SystemFault implements TransactionError {

    long xid;

    Throwable throwable;

    public SystemFault() {
        super();
    }

    public SystemFault(long xid, Throwable throwable) {
        super();
        this.xid = xid;
        this.throwable = throwable;
    }

    public long getXid() {
        return xid;
    }

    public Throwable getThrowable() {
        return throwable;
    }

    public void setXid(long xid) {
        this.xid = xid;
    }

    public void setThrowable(Throwable throwable) {
        this.throwable = throwable;
    }
}
