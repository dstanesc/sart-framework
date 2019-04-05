package org.sartframework.session;

public class SystemTransaction {

    String sid;
    
    Long xid;

    public SystemTransaction() {
        super();
    }
    
    public SystemTransaction(String sid, Long xid) {
        super();
        this.sid = sid;
        this.xid = xid;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public Long getXid() {
        return xid;
    }

    public void setXid(Long xid) {
        this.xid = xid;
    }

    @Override
    public String toString() {
        return "SystemTransaction [sid=" + sid + ", xid=" + xid + "]";
    }

}
