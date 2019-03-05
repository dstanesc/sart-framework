package org.sartframework.event;

public class GenericEvent implements Event {

    long xid;
    
    long creationTime;

    public GenericEvent() {
        super();
    }

    public GenericEvent(long xid) {
        super();
        this.xid = xid;
        this.creationTime = System.currentTimeMillis();
    }

    public long getXid() {
        return xid;
    }
    
    @Override
    public long getCreationTime() {
        return creationTime;
    }

    public void setCreationTime(long creationTime) {
        this.creationTime = creationTime;
    }

    public void setXid(long xid) {
        this.xid = xid;
    }
}
